package com.gameserver.responses;


public record Response(boolean success, String message) {
    public static final Response OK = new Response(true, "ok");
}
