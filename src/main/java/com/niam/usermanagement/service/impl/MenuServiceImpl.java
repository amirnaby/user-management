package com.niam.usermanagement.service.impl;

import com.niam.common.exception.EntityNotFoundException;
import com.niam.common.exception.ResultResponseStatus;
import com.niam.usermanagement.model.entities.Menu;
import com.niam.usermanagement.model.entities.Permission;
import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.repository.MenuRepository;
import com.niam.usermanagement.service.JwtService;
import com.niam.usermanagement.service.MenuService;
import com.niam.usermanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {
    private final MenuRepository menuRepository;
    private final UserService userService;
    private final JwtService jwtService;

    @Override
    public List<Menu> getMenusForCurrentUser(HttpServletRequest request) {
        String token = jwtService.getJwtFromRequest(request);
        if (token == null) return List.of();
        String username = jwtService.extractUsername(token);
        User user = (User) userService.loadUserByUsername(username);
        Set<String> userPermissions = getUserPermissions(user);
        return menuRepository.findAll().stream()
                .filter(menu -> menu.getPermissions().stream()
                        .anyMatch(userPermissions::contains))
                .collect(Collectors.toList());
    }

    @Override
    public Menu getMenuById(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ResultResponseStatus.ENTITY_NOT_FOUND.getResponseCode(),
                        ResultResponseStatus.ENTITY_NOT_FOUND.getReasonCode(),
                        "Menu with id=" + id + " not found"
                ));
    }

    @Override
    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }

    private Set<String> getUserPermissions(User user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    @Transactional
    @Override
    public List<Menu> createMenus(List<Menu> menus) {
        return menuRepository.saveAll(menus);
    }

    @Transactional
    @Override
    public List<Menu> updateMenus(List<Menu> menus) {
        List<Menu> updated = new ArrayList<>();
        for (Menu menu : menus) {
            Menu existing = menuRepository.findById(menu.getId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            ResultResponseStatus.ENTITY_NOT_FOUND.getResponseCode(),
                            ResultResponseStatus.ENTITY_NOT_FOUND.getReasonCode(),
                            "Menu with id=" + menu.getId() + " not found"
                    ));
            BeanUtils.copyProperties(menu, existing, "id");
            updated.add(existing);
        }
        return menuRepository.saveAll(updated);
    }

    @Transactional("transactionManager")
    @Override
    public void deleteMenu(Long id) {
        Menu existing = getMenuById(id);
        menuRepository.delete(existing);
    }
}