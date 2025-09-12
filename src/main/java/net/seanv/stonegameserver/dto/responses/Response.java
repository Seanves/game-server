package net.seanv.stonegameserver.dto.responses;


public record Response(boolean success, String message) {
    public static final Response OK                     = new Response(true, "ok"),
                                 NOT_IN_QUEUE           = new Response(false, "not in queue"),
                                 NOT_IN_ACCEPTANCE      = new Response(false, "not in acceptance"),
                                 ALREADY_IN_QUEUE       = new Response(false, "already in queue"),
                                 ALREADY_IN_ACCEPTANCE  = new Response(false, "already in acceptance"),
                                 ALREADY_IN_GAME        = new Response(false, "already in game");

    public Response(boolean success) { this(success, ""); }
}
