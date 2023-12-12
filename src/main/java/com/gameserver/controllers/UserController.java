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

@RestController
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/changeNick")
    public void changeNick(@RequestBody String nick) {
        userService.changeNickname(((MyUserDetails)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUser(), nick);
    }

    @PostMapping("/stats")
    public Stats stats() {
        User user = getUser();
        return new Stats(user, userService.getRank(user));
    }


    private User getUser() {
        return ((MyUserDetails)(SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUser();
    }
}
