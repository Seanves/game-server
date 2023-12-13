package com.gameserver.entities.auth;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDto {
    @Size(min = 5, max = 20, message = "Login must be from 5 to 20 characters long")
    private String login;
    @Size(min = 5, max = 30, message = "Password must be from 5 to 30 characters long")
    private String password;
    @Size(min = 5, max = 20, message = "Nickname must be from 5 to 20 characters long")
    private String nickname;
}
