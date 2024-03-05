package com.gameserver.controllers;

import com.gameserver.entities.auth.UserDTO;
import com.gameserver.entities.responses.GameResultDTO;
import com.gameserver.entities.User;
import com.gameserver.entities.responses.Response;
import com.gameserver.entities.responses.UserInfo;
import com.gameserver.security.MyUserDetails;
import com.gameserver.services.UserService;
import com.gameserver.util.UserDTOValidator;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class UserController {

    private final UserService userService;
    private final UserDTOValidator userDTOValidator;
    private final Validator defaultValidator;

    @Autowired
    public UserController(UserService userService, UserDTOValidator userDTOValidator,
                                                           Validator defaultValidator) {
        this.userService = userService;
        this.userDTOValidator = userDTOValidator;
        this.defaultValidator = defaultValidator;
    }


    @PostMapping("/userInfo")
    public UserInfo userInfo() {
        return userService.getUserInfo(getUser());
    }

    @PostMapping("/results")
    public Page<GameResultDTO> gameResults(@RequestBody int page) {
        return userService.getGameResults(getUser(), page);
    }

    @PostMapping("/top10")
    public List<UserInfo> top10Ranks() {
        return userService.getTop10Ranks();
    }

    @PostMapping("/changeNickname")
    public Response changeNickname(@RequestBody String newNickname, BindingResult br) {
        var violations = defaultValidator.validateValue(UserDTO.class, "nickname", newNickname);
        violations.forEach( v -> br.addError(new FieldError("", "nickname", v.getMessage())));

        userDTOValidator.validateNickname(newNickname, br);

        if(br.hasErrors()) {
            return new Response(false, "Errors: " + br.getAllErrors()
                                 .stream().map(error -> error.getDefaultMessage()).toList());
        }
        return userService.changeNickname(getUser(), newNickname);
    }


    private User getUser() {
        return ((MyUserDetails)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUser();
    }
}
