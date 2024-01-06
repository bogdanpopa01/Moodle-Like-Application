package com.ing.hubs.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@AllArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private JwtAuthorizationFilter jwtAuthorizationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/users/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/users/me/grades").hasRole("STUDENT")
                                .requestMatchers(HttpMethod.GET, "/users/me/schedules").hasRole("STUDENT")
                                .requestMatchers("/courses/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/courses").hasRole("TEACHER")
                                .requestMatchers(HttpMethod.DELETE, "/courses/*").hasRole("TEACHER")
                                .requestMatchers(HttpMethod.PATCH, "/courses/*").hasRole("TEACHER")
                                .requestMatchers("/enrollments/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/enrollments/*").hasRole("STUDENT")
                                .requestMatchers(HttpMethod.PATCH, "/enrollments/grades/*").hasRole("TEACHER")
                                .requestMatchers(HttpMethod.PATCH, "/enrollments/*").hasRole("TEACHER")
                                .requestMatchers(HttpMethod.GET, "/enrollments/courses/*").hasRole("TEACHER")
                                .requestMatchers("/schedules/**").permitAll()
                                .anyRequest().authenticated()
                )
                .sessionManagement(httpSecuritySessionManagementConfigurer
                        -> httpSecuritySessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
