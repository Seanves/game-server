package net.seanv.stonegameserver.dto.auth;

public record AuthResponse(boolean success, String message, String token) {
}
