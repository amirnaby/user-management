package com.niam.usermanagement.service;

import com.niam.usermanagement.model.entities.Menu;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MenuService {
    List<Menu> getMenusForCurrentUser(HttpServletRequest request);

    @Transactional
    Menu createMenu(Menu menu);

    @Transactional
    Menu updateMenu(Long id, Menu updated);

    @Transactional
    void deleteMenu(Long id);

    Menu getMenuById(Long id);

    List<Menu> getAllMenus();
}