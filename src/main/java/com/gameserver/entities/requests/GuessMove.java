package com.gameserver.entities.requests;

import lombok.Data;

@Data
public class GuessMove {
    private int id;
    private boolean even;
}