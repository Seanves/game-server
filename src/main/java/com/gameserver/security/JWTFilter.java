package com.gameserver.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.gameserver.services.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTManager jwtManager;
    private final AuthenticationService authenticationService;

    @Autowired
    public JWTFilter(JWTManager jwtManager, AuthenticationService authenticationService) {
        this.jwtManager = jwtManager;
        this.authenticationService = authenticationService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if(auth != null && !auth.isBlank() && auth.startsWith("Bearer ")) {
            String jwt = auth.substring(7);
            if(jwt.isBlank()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Wrong jwt token");
                return;
            }
            else {
                try {
                    int id = jwtManager.validateAndGetId(jwt);
                    UserDetails userDetails = authenticationService.loadUserById(id);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                         userDetails, userDetails.getPassword(), userDetails.getAuthorities());

                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } catch(Exception e) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Wrong jwt token");
                    return;
                }
            }
        }

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
