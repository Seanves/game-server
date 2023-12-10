package com.gameserver.entities.auth;

import lombok.Data;

@Data
public class UserDto {
    private String login;
    private String password;
}
