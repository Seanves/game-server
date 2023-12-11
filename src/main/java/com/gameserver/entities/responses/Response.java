package com.gameserver.entities.responses;


public record Response(boolean success, String message) {
    public Response(boolean success) { this(success, ""); }
    public static final Response OK = new Response(true, "ok");
}
