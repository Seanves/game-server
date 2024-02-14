package com.gameserver.controllers;

import com.gameserver.entities.GameResultDTO;
import com.gameserver.entities.User;
import com.gameserver.entities.responses.Response;
import com.gameserver.entities.responses.UserInfo;
import com.gameserver.security.MyUserDetails;
import com.gameserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/changeNickname")
    public Response changeNickname(@RequestBody String newNickname) {
        return userService.changeNickname(getUser(), newNickname);
    }

    @PostMapping("/userInfo")
    public UserInfo userInfo() {
        return userService.getUserInfo(getUser());
    }

    @PostMapping("/top10")
    public List<UserInfo> top10Ranks() {
        return userService.getTop10Ranks();
    }

    @PostMapping("/results")
    public Page<GameResultDTO> gameResults(@RequestBody int page) {
        return userService.getGameResults(getUser(), page);
    }


    private User getUser() {
        return ((MyUserDetails)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUser();
    }
}
