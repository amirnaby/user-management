package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.Menu;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface MenuService {
    List<Menu> getMenusForCurrentUser(HttpServletRequest request);

    Menu createMenu(Menu menu);

    Menu updateMenu(Long id, Menu updated);

    void deleteMenu(Long id);

    Menu getMenuById(Long id);

    List<Menu> getAllMenus();
}