package com.gameserver.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class JWTManager {

    @Value("${jwt_secret}")
    private String secret;


    public String generate(int id) {
        Date expDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());

        return JWT.create()
                  .withSubject("User details")
                  .withClaim("id", id)
                  .withIssuedAt(new Date())
                  .withIssuer("server")
                  .withExpiresAt(expDate)
                  .sign(Algorithm.HMAC256(secret));
    }

    public int  validateAndGetId(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                                  .withSubject("User details")
                                  .withIssuer("server")
                                  .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("id").asInt();
    }

}
