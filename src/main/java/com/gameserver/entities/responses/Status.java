package com.gameserver.entities.responses;

public record Status(boolean inQueue, boolean inAcceptance, boolean inGame) {}
