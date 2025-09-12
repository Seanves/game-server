package net.seanv.stonegameserver.dto.auth;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthDTO {
    @Size(min = 5, max = 20, message = "Login must be from 5 to 20 characters long")
    private String login;
    @Size(min = 5, max = 30, message = "Password must be from 5 to 30 characters long")
    private String password;
    @Size(min = 5, max = 20, message = "Nickname must be from 5 to 20 characters long")
    private String nickname;
}
