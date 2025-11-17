package com.niam.usermanagement.service.otp.store;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryOtpStore implements OtpStore {
    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    @Override
    public void saveOtp(String username, String otp, long ttlSeconds) {
        Entry e = new Entry();
        e.otp = otp;
        e.expireAt = Instant.now().plusSeconds(ttlSeconds);
        store.put(username, e);
    }

    @Override
    public String getOtp(String username) {
        Entry e = store.get(username);
        if (e == null || e.expireAt.isBefore(Instant.now())) {
            store.remove(username);
            return null;
        }
        return e.otp;
    }

    @Override
    public void removeOtp(String username) {
        store.remove(username);
    }

    private static class Entry {
        String otp;
        Instant expireAt;
    }
}