package com.niam.usermanagement.service.impl;

import com.niam.usermanagement.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {
    private final CacheManager cacheManager;

    /**
     * Returns a list of all cache names currently managed by Spring.
     */
    @Override
    public List<String> getAllCacheNames() {
        return new ArrayList<>(cacheManager.getCacheNames());
    }

    /**
     * Clears all entries from all caches.
     */
    @Override
    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        });
    }

    /**
     * Clears a specific cache by name.
     */
    @Override
    public void clearCacheByName(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalArgumentException("Cache not found: " + cacheName);
        }
        cache.clear();
    }

    /**
     * Gets all current keys in a specific cache (only works for caches that support it, like ConcurrentMapCache).
     */
    @Override
    public List<Object> getCacheKeys(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalArgumentException("Cache not found: " + cacheName);
        }

        // Spring’s default ConcurrentMapCache exposes internal store via native cache
        Object nativeCache = cache.getNativeCache();
        if (nativeCache instanceof Map<?, ?> map) {
            return new ArrayList<>(map.keySet());
        }
        return Collections.emptyList();
    }

    /**
     * Returns basic cache info: name + number of entries (for supported caches).
     */
    @Override
    public Map<String, Integer> getCacheStats() {
        return cacheManager.getCacheNames().stream().collect(Collectors.toMap(
                name -> name,
                name -> {
                    Cache cache = cacheManager.getCache(name);
                    if (cache != null && cache.getNativeCache() instanceof Map<?, ?> map) {
                        return map.size();
                    }
                    return -1;
                }
        ));
    }
}