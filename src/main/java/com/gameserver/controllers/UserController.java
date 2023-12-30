package com.gameserver.controllers;

import com.gameserver.entities.User;
import com.gameserver.entities.responses.Stats;
import com.gameserver.security.MyUserDetails;
import com.gameserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void changeNick(@RequestBody String nickname) {
        userService.changeNickname(getUser(), nickname);
    }

    @PostMapping("/stats")
    public Stats stats() {
        return userService.getStats(getUser());
    }

    @PostMapping("/top10")
    public List<Object[]> top10Ranks() {
        return userService.getTop10Ranks();
    }


    private User getUser() {
        return ((MyUserDetails)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUser();
    }
}
