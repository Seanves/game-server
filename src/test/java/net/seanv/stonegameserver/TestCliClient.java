package net.seanv.stonegameserver;

import net.seanv.stonegameserver.dto.auth.*;
import net.seanv.stonegameserver.dto.responses.*;
import net.seanv.stonegameserver.game.GameSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.Scanner;


public class TestCliClient {

    private final RestTemplate restTemplate = new RestTemplate();
//    private final String SERVER_ADDRESS = "https://game-server-kaflujbsaq-lz.a.run.app";
    private final String SERVER_ADDRESS = "http://localhost:8080";
    private String token;
    private final Scanner scanner = new Scanner(System.in);


    public void run() {

        // register
        String regChoose = scanNext("Do you want to register[Y/N]");
        if (regChoose.equals("Y") || regChoose.equals("y")) {
            AuthResponse response;
            do {
                String login = scanNext("Enter login");
                String password = scanNext("Enter password");
                String nickname = scanNext("Enter nickname");
                response = register(login, password, nickname);
                System.out.println(response.message());
            } while (!response.success());

        }

        // login
        do {
            String login = scanNext("Enter login");
            String password = scanNext("Enter password");
            AuthResponse response = login(login, password);
            System.out.println(response.message());
        } while (token == null);

        System.out.println(postRequest("/userInfo", null, UserInfo.class));
        System.out.println(postRequest("/results", 1, String.class));

        // search if not in game, otherwise reconnect
        if (!postRequest("/userStatus", UserStatus.class).inGame()) {

            // searching game
            postRequest("/findGame");
            do {
                System.out.println("searching game...");
                notifyingPostRequest("/notifyWhenFound");
            } while (postRequest("/userStatus", UserStatus.class).inQueue());

            if (!postRequest("/userStatus", UserStatus.class).inAcceptance()) {
                System.out.println("Timeout from queue");
                System.exit(0);
            }

            // accept/decline game
            do {
                String choose = scanNext("\nGame found, accept[Y/N]");
                if (choose.equals("Y") || choose.equals("y")) {
                    postRequest("/accept", Response.class);
                    break;
                }
                else if (choose.equals("N") || choose.equals("n")) {
                    postRequest("/decline", Response.class);
                    System.exit(0);
                }
            } while (postRequest("/userStatus", UserStatus.class).inAcceptance());

            do {
                System.out.println("Waiting other player to accept...");
                notifyingPostRequest("/notifyWhenNotInAcceptance");
//                Thread.sleep(1000);
            } while (postRequest("/userStatus", UserStatus.class).inAcceptance());


            if (!postRequest("/userStatus", UserStatus.class).inGame()) {
                System.out.println("Opponent declined");
                System.exit(0);
            }


            System.out.println("\nGame started, your opponent: " + postRequest("/opponent", Opponent.class));
        }

        // playing game
        do {
            GameResponse response = postRequest("/gameStatus", GameResponse.class);
            if (response.yourTurn()) {

                if (response.turnType() == GameSession.TurnType.GUESSING) {
                    String choose = scanNext("Even or not[Y/N]");
                    GameResponse subresponse = switch (choose) {
                        case "Y","y" -> postRequest("/makeGuess", true, GameResponse.class);
                        case "N","n" -> postRequest("/makeGuess", false, GameResponse.class);
                        default -> throw new IllegalArgumentException(choose);
                    };

                    // check if guessed
                    if (subresponse != null && subresponse.success()) {
                        subresponse = postRequest("/gameStatus", GameResponse.class);

                        int before = response.yourPoints(),
                                after = subresponse.yourPoints();

                        if (after < before) {
                            System.out.println("You didn't guess, -" + (before-after) + " points");
                        }
                        if (after > before) {
                            System.out.println("You guessed, +" + (after-before) + " points");
                        }
                    } else { System.out.println(subresponse.message()); }
                }

                else if (response.turnType() == GameSession.TurnType.CHOOSING) {
                    System.out.print("Make choose (you have " + response.yourPoints() + "): ");
                    int choose = scanner.nextInt();
                    GameResponse subresponse = postRequest("/makeChoose", choose, GameResponse.class);

                    // check if opponent guessed
                    if (subresponse != null && subresponse.success()) {
                        notifyingPostRequest("/notifyWhenTurnChanged");
                        subresponse = postRequest("/gameStatus", GameResponse.class);

                        int before = response.yourPoints(),
                                after = subresponse.yourPoints();

                        if (after < before) {
                            System.out.println("Opponent guessed, -" + (before-after) + " points");
                        }
                        if (after > before) {
                            System.out.println("Opponent didn't guess, +" + (after-before) + " points");
                        }

                        notifyingPostRequest("/notifyWhenTurnChanged ");
                    } else { System.out.println(subresponse.message()); }
                }

            } else { notifyingPostRequest("/notifyWhenTurnChanged "); }


            if (response.gameOver()) {
                PostGameResult result = postRequest("/leaveGame", PostGameResult.class);
                int ratingChange = result.currentRating() - result.prevRating();
                System.out.printf("\n%s, rating %d -> %d (%s%d)\n", result.win() ? "You won" : "You lost",
                                            result.prevRating(), result.currentRating(),
                                            ratingChange >= 0 ? "+" : "", ratingChange);
                break;
            }

        } while (postRequest("/userStatus", UserStatus.class).inGame());

    }

    public static void main(String[] args) {
        TestCliClient client = new TestCliClient();
        client.run();
    }


    private String scanNext(String message) {
        System.out.print(message + ": ");
        return scanner.next();
    }

    private AuthResponse login(String login, String password) {
        AuthResponse authResponse = postRequest("/login", new UserAuthDTO(login, password, ""), AuthResponse.class, true);
        if (authResponse.success()) {
            token = authResponse.token();
        }
        return authResponse;
    }

    private AuthResponse register(String login, String password, String nickname) {
        return postRequest("/register", new UserAuthDTO(login, password, nickname), AuthResponse.class, true);
    }


    private String postRequest(String path) {
        return postRequest(path, null, String.class, false);
    }

    private <R> R postRequest(String path, Class<R> responseClass) {
        return postRequest(path, null, responseClass, false);
    }

    private <O> String postRequest(String path, O object) {
        return postRequest(path, object, String.class, false);
    }

    private <O,R> R postRequest(String path, O object, Class<R> responseClass) {
        return postRequest(path, object, responseClass, false);
    }

    private <O,R> R postRequest(String path, O object, Class<R> responseClass, boolean authRequest) {
        if (token == null && !authRequest) { throw new RuntimeException("not authorized"); }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (!authRequest) {
            headers.add("Authorization","Bearer " + token);
        }

        HttpEntity<O> requestEntity = new HttpEntity<>(object, headers);
        ResponseEntity<R> responseEntity =
                restTemplate.postForEntity(SERVER_ADDRESS + path, requestEntity, responseClass);

        R body = responseEntity.getBody();

        return body;
    }

    // for long polling
    public void notifyingPostRequest(String url) {
        try {
            postRequest(url);
        } catch (Exception e) {}
    }

}
