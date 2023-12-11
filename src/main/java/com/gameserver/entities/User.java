package com.gameserver.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

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



    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) { return true; }
        if(o == null) { return false; }
        if(o instanceof Integer && ((Integer) o).intValue() == this.id) { return true; }
        if(getClass() != o.getClass()){ return false; }
        User other = (User) o;
        return this.id == other.id;
    }
}
