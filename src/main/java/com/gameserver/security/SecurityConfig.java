package com.gameserver.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    private final UserDetailsService userDetailsService;
    private final JWTFilter jwtFilter;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, JWTFilter jwtFilter) {
//        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable().authorizeHttpRequests()
                .requestMatchers("/login","/register", "/error").permitAll()
                .anyRequest().authenticated()
//                .and().formLogin().loginPage("/logins")
//                .loginProcessingUrl("/process_login")
//                .defaultSuccessUrl("/", true).permitAll()
//                .and().logout().logoutUrl("/process_logout")
//                .logoutSuccessUrl("/login")
                .and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

//    @Bean
//    public AuthenticationManager authenticationManager(
//            AuthenticationConfiguration configuration) throws Exception {
//        return configuration.getAuthenticationManager();
//    }

    @Bean
    public PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }

}
