package net.seanv.stonegameserver.dto.auth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.seanv.stonegameserver.dto.validation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldsNotEqual(
    field1 = "password",
    field2 = "login"
)
@FieldsNotEqual(
    field1 = "password",
    field2 = "nickname"
)
public class UserAuthDTO {
    @NotNull
    @Size(min = 5, max = 20, message = "Login must be from 5 to 20 characters long")
    @WithoutSpaces
    @LoginIsNotTaken
    private String login;

    @NotNull
    @Size(min = 5, max = 30, message = "Password must be from 5 to 30 characters long")
    @StrongPassword
    private String password;

    @NotNull
    @Size(min = 5, max = 20, message = "Nickname must be from 5 to 20 characters long")
    @WithoutSpaces
    @MaxCaps(percentage = 50)
    private String nickname;
}
