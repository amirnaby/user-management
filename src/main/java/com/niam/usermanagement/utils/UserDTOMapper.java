package com.niam.usermanagement.utils;

import com.niam.usermanagement.model.entities.Role;
import com.niam.usermanagement.model.entities.User;
import com.niam.usermanagement.model.payload.request.UserDTO;
import com.niam.usermanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDTOMapper {
    private final UserService userService;

    public User userDTOToUser(UserDTO userDTO) {
        return userService.loadUserByUsername(userDTO.getUsername());
    }

    public UserDTO userToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        userDTO.setRoleNames(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        return userDTO;
    }
}
