package com.suraj.security.hospitalManagement.security;

import com.suraj.security.hospitalManagement.entity.type.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrfConfigurer -> csrfConfigurer.disable())
                .sessionManagement(sessionConfigurer -> sessionConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**", "/auth/**").permitAll()
                        .requestMatchers("/admin/**").hasRole(RoleType.ADMIN.name())
                        .requestMatchers("/doctors/**").hasAnyRole(RoleType.ADMIN.name(), RoleType.DOCTOR.name())
                        .anyRequest().authenticated()
                )
                .addFilterAfter(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oAuth2 -> oAuth2
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 Authentication Failed: {}", exception.getMessage());
                            response.setStatus(401);
                            handlerExceptionResolver.resolveException(request, response, null, exception);
                        })
                        .successHandler(oAuth2SuccessHandler)
                )
                .exceptionHandling(exceptionHandlingConfigurer -> {
                    exceptionHandlingConfigurer.accessDeniedHandler(((request, response, accessDeniedException) ->
                            handlerExceptionResolver.resolveException(request, response, null, accessDeniedException)
                            ));
                });
//                .formLogin(Customizer.withDefaults());

        return httpSecurity.build();
    }

}
