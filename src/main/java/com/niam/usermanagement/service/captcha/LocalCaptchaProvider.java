package com.niam.usermanagement.service.captcha;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
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
 * NOTE:
 * - Suitable for dev / single instance deployments.
 * - For production, replace store with Redis or any TTL-backed cache.
 */
@Service("localCaptchaProvider")
public class LocalCaptchaProvider implements CaptchaProvider {
    /**
     * token -> captcha entry (text + expiry)
     * One-time-use: entry is removed after successful validation.
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

    /**
     * Initialize Kaptcha instance after properties are injected.
     */
    @PostConstruct
    public void init() {
        this.kaptcha = buildKaptcha();
    }

    /**
     * Build and configure DefaultKaptcha generator.
     */
    private DefaultKaptcha buildKaptcha() {
        DefaultKaptcha k = new DefaultKaptcha();
        Properties props = new Properties();

        props.put("kaptcha.image.width", imgWidth);
        props.put("kaptcha.image.height", imgHeight);
        props.put("kaptcha.textproducer.char.length", charLength);

        // Prevent OCR attacks by using mixed chars
        props.put("kaptcha.textproducer.char.string", "abcde2345678gfynmnpwx");

        props.put("kaptcha.background.clear.from", "lightGray");
        props.put("kaptcha.background.clear.to", "white");

        k.setConfig(new Config(props));
        return k;
    }

    @Override
    public CaptchaResponse generate() {
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
    public boolean validate(String token, String userResponse) {
        if (token == null || userResponse == null) return false;

        CaptchaEntry entry = store.get(token);
        if (entry == null) return false;

        // expired?
        if (Instant.now().isAfter(entry.expiresAt())) {
            store.remove(token);
            return false;
        }

        boolean ok = entry.text().equalsIgnoreCase(userResponse.trim());

        if (ok) store.remove(token); // enforce one-time usage

        return ok;
    }

    /**
     * Simple record to store captcha text + expiry time
     */
    private record CaptchaEntry(String text, Instant expiresAt) {
    }
}