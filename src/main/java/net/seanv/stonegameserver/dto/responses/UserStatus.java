package net.seanv.stonegameserver.dto.responses;

public record UserStatus(boolean inQueue, boolean inAcceptance, boolean inGame) {}
