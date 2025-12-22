package com.niam.usermanagement.controller;

import com.niam.common.model.response.ServiceResponse;
import com.niam.common.utils.ResponseEntityUtil;
import com.niam.usermanagement.annotation.HasPermission;
import com.niam.usermanagement.model.enums.PRIVILEGE;
import com.niam.usermanagement.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/cache")
public class CacheController {
    private final CacheService cacheService;
    private final ResponseEntityUtil responseEntityUtil;

    @HasPermission(PRIVILEGE.CACHE_MANAGE)
    @GetMapping
    public ResponseEntity<ServiceResponse> getAllCaches() {
        List<String> caches = cacheService.getAllCacheNames();
        return responseEntityUtil.ok(caches);
    }

    @HasPermission(PRIVILEGE.CACHE_MANAGE)
    @GetMapping("/stats")
    public ResponseEntity<ServiceResponse> getCacheStats() {
        Map<String, Integer> stats = cacheService.getCacheStats();
        return responseEntityUtil.ok(stats);
    }

    @HasPermission(PRIVILEGE.CACHE_MANAGE)
    @DeleteMapping
    public ResponseEntity<ServiceResponse> clearAllCaches() {
        cacheService.clearAllCaches();
        return responseEntityUtil.ok("All caches cleared successfully");
    }

    @HasPermission(PRIVILEGE.CACHE_MANAGE)
    @DeleteMapping("/{name}")
    public ResponseEntity<ServiceResponse> clearCacheByName(@PathVariable String name) {
        cacheService.clearCacheByName(name);
        return responseEntityUtil.ok("Cache '" + name + "' cleared successfully");
    }

    @HasPermission(PRIVILEGE.CACHE_MANAGE)
    @GetMapping("/{name}/keys")
    public ResponseEntity<ServiceResponse> getCacheKeys(@PathVariable String name) {
        List<Object> keys = cacheService.getCacheKeys(name);
        return responseEntityUtil.ok(keys);
    }
}