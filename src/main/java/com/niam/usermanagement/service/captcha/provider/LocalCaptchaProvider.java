package com.niam.usermanagement.service.captcha.provider;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.niam.usermanagement.model.payload.request.CaptchaGenerateRequest;
import com.niam.usermanagement.model.payload.response.CaptchaResponse;
import com.niam.usermanagement.model.payload.request.CaptchaValidateRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local (in-memory) captcha provider using Kaptcha library.
 * Stores token → (captchaText, expiresAt) in an in-memory map.
 * Suitable for development / single-instance environments.
 */
@Service("localCaptchaProvider")
public class LocalCaptchaProvider implements CaptchaProvider {
    /**
     * token -> captcha entry (text + expiry)
     */
    private final Map<String, CaptchaEntry> store = new ConcurrentHashMap<>();

    private DefaultKaptcha kaptcha;

    @Value("${captcha.local.image.width:200}")
    private String imgWidth;

    @Value("${captcha.local.image.height:50}")
    private String imgHeight;

    @Value("${captcha.local.length:6}")
    private String charLength;

    @Value("${captcha.ttl.seconds:120}")
    private int ttlSeconds;

    @PostConstruct
    public void init() {
        this.kaptcha = buildKaptcha();
    }

    private DefaultKaptcha buildKaptcha() {
        DefaultKaptcha k = new DefaultKaptcha();
        Properties props = new Properties();

        props.put("kaptcha.image.width", imgWidth);
        props.put("kaptcha.image.height", imgHeight);
        props.put("kaptcha.textproducer.char.length", charLength);

        // Random text set to avoid pattern detection
        props.put("kaptcha.textproducer.char.string", "abcde2345678gfynmnpwx");

        props.put("kaptcha.background.clear.from", "lightGray");
        props.put("kaptcha.background.clear.to", "white");

        k.setConfig(new Config(props));
        return k;
    }

    @Override
    public CaptchaResponse generate(CaptchaGenerateRequest request) {
        String text = kaptcha.createText();
        BufferedImage image = kaptcha.createImage(text);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);

            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            String token = UUID.randomUUID().toString();
            Instant expiresAt = Instant.now().plusSeconds(ttlSeconds);

            store.put(token, new CaptchaEntry(text, expiresAt));

            return new CaptchaResponse(token, base64, ttlSeconds);

        } catch (IOException ex) {
            throw new RuntimeException("Failed to generate captcha", ex);
        }
    }

    @Override
    public boolean validate(CaptchaValidateRequest request) {
        String token = request.token();
        String userResponse = request.response();

        if (token == null || userResponse == null) return false;

        CaptchaEntry entry = store.get(token);
        if (entry == null) return false;

        // expired?
        if (Instant.now().isAfter(entry.expiresAt())) {
            store.remove(token);
            return false;
        }

        boolean ok = entry.text().equalsIgnoreCase(userResponse.trim());

        if (ok) store.remove(token); // one-time use

        return ok;
    }

    /**
     * Simple record to store captcha text + expiry time.
     */
    private record CaptchaEntry(String text, Instant expiresAt) {
    }
}