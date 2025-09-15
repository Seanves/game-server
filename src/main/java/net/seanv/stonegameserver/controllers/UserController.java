package net.seanv.stonegameserver.controllers;

import net.seanv.stonegameserver.dto.auth.UserAuthDTO;
import net.seanv.stonegameserver.dto.responses.GameResultDTO;
import net.seanv.stonegameserver.dto.responses.Response;
import net.seanv.stonegameserver.dto.responses.UserInfo;
import net.seanv.stonegameserver.security.AuthUserContext;
import net.seanv.stonegameserver.services.UserService;
import jakarta.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class UserController {

    private final UserService userService;
    private final Validator validator;
    private final AuthUserContext auth;


    public UserController(UserService userService, Validator validator, AuthUserContext auth) {
        this.userService = userService;
        this.validator = validator;
        this.auth = auth;
    }


    @PostMapping("/userInfo")
    public UserInfo userInfo() {
        return userService.getUserInfo(auth.loadUser());
    }

    @PostMapping("/results")
    public Page<GameResultDTO> gameResults(@RequestBody int page) {
        return userService.getGameResults(auth.getId(), page);
    }

    @PostMapping("/top10")
    public List<UserInfo> top10Ranks() {
        return userService.getTop10Ranks();
    }

    @PostMapping("/changeNickname")
    public Response changeNickname(@RequestBody String newNickname, BindingResult br) {
        var violations = validator.validateValue(UserAuthDTO.class, "nickname", newNickname);
        violations.forEach( v -> br.addError(new FieldError("", "nickname", v.getMessage())));

        if (br.hasErrors()) {
            return new Response(false, br.getAllErrors()
                                                .stream()
                                                .map(e -> e.getDefaultMessage())
                                                .toList()
                                                .toString());
        }

        userService.changeNickname(auth.loadUser(), newNickname);
        return Response.OK;
    }


}
