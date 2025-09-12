package net.seanv.stonegameserver.dto.responses;

public record PostGameResult(boolean win,
                             int currentRating,
                             int prevRating) {
}
