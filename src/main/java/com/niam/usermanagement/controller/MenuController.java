package com.niam.usermanagement.controller;

import com.niam.common.model.response.ServiceResponse;
import com.niam.common.utils.ResponseEntityUtil;
import com.niam.usermanagement.model.entities.Menu;
import com.niam.usermanagement.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;
    private final ResponseEntityUtil responseEntityUtil;

    @GetMapping("/current")
    public ResponseEntity<ServiceResponse> getMenusForCurrentUser(HttpServletRequest request) {
        return responseEntityUtil.ok(menuService.getMenusForCurrentUser(request));
    }

    @GetMapping
    public ResponseEntity<ServiceResponse> getAllMenus() {
        return responseEntityUtil.ok(menuService.getAllMenus());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getMenu(@PathVariable Long id) {
        return responseEntityUtil.ok(menuService.getMenuById(id));
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> createMenu(@RequestBody Menu menu) {
        return responseEntityUtil.ok(menuService.createMenu(menu));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateMenu(@PathVariable Long id, @RequestBody Menu menu) {
        return responseEntityUtil.ok(menuService.updateMenu(id, menu));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResponse> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return responseEntityUtil.ok("Menu has been deleted!");
    }
}