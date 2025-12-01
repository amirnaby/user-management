package com.niam.usermanagement.controller;

import com.niam.common.model.response.ServiceResponse;
import com.niam.common.utils.ResponseEntityUtil;
import com.niam.usermanagement.model.entities.Menu;
import com.niam.usermanagement.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;
    private final ResponseEntityUtil responseEntityUtil;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/current")
    public ResponseEntity<ServiceResponse> getMenusForCurrentUser(HttpServletRequest request) {
        return responseEntityUtil.ok(menuService.getMenusForCurrentUser(request));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ServiceResponse> getAllMenus() {
        return responseEntityUtil.ok(menuService.getAllMenus());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getMenu(@PathVariable Long id) {
        return responseEntityUtil.ok(menuService.getMenuById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/batch")
    public ResponseEntity<ServiceResponse> createMenus(@RequestBody List<Menu> menus) {
        return responseEntityUtil.ok(menuService.createMenus(menus));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/batch")
    public ResponseEntity<ServiceResponse> updateMenus(@RequestBody List<Menu> menus) {
        return responseEntityUtil.ok(menuService.updateMenus(menus));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResponse> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return responseEntityUtil.ok("Menu has been deleted!");
    }
}