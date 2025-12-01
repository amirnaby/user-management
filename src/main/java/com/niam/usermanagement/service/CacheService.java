package com.niam.usermanagement.service;

import java.util.List;
import java.util.Map;

public interface CacheService {
    List<String> getAllCacheNames();

    void clearAllCaches();

    void clearCacheByName(String cacheName);

    List<Object> getCacheKeys(String cacheName);

    Map<String, Integer> getCacheStats();
}