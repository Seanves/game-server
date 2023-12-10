package com.gameserver.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity @Data
@Table(name="Users")
@NoArgsConstructor
public class User {

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Id @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;
}
