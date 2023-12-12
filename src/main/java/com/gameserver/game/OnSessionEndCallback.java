package com.gameserver.game;

import com.gameserver.entities.User;

public interface OnSessionEndCallback {
    int onSessionEnd(GameSession game);
}
