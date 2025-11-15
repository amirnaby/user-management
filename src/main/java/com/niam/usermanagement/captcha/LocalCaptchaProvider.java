package com.niam.usermanagement.captcha;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple local captcha provider using kaptcha.
 * Stores token -> (value, expiresAt) in memory (ConcurrentHashMap).
 * In production prefer Redis or a persistent TTL store.
 */
@Service("localCaptchaProvider")
public class LocalCaptchaProvider implements CaptchaProvider {

    private DefaultKaptcha kaptcha;

    private static class CaptchaEntry {
        final String text;
        final Instant expiresAt;
        CaptchaEntry(String text, Instant expiresAt) {
            this.text = text; this.expiresAt = expiresAt;
        }
    }

    // simple in-memory store
    private final Map<String, CaptchaEntry> store = new ConcurrentHashMap<>();

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
        props.put("kaptcha.textproducer.char.string", "abcde2345678gfynmnpwx");
        props.put("kaptcha.background.clear.from", "lightGray");
        props.put("kaptcha.background.clear.to", "white");
        Config config = new Config(props);
        k.setConfig(config);
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
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate captcha", ex);
        }
    }

    @Override
    public boolean validate(String token, String userResponse) {
        if (token == null || userResponse == null) return false;
        CaptchaEntry e = store.get(token);
        if (e == null) return false;
        if (Instant.now().isAfter(e.expiresAt)) {
            store.remove(token);
            return false;
        }
        boolean ok = e.text.equalsIgnoreCase(userResponse.trim());
        if (ok) store.remove(token); // one-time use
        return ok;
    }
}