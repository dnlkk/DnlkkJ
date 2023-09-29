package com.dnlkk.WebTest.dtos;

import com.dnlkk.WebTest.model.User;
import lombok.Data;

@Data
public class UserResponseDTO {
    private UserDTO user;
    public UserResponseDTO(UserDTO user) {
        this.user = user;
    }
}
