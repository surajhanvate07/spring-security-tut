package com.suraj.security.hospitalManagement.security;

import com.suraj.security.hospitalManagement.entity.User;
import com.suraj.security.hospitalManagement.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final AuthUtil authUtil;
    private final HandlerExceptionResolver exceptionResolver;

    public JwtAuthFilter(UserRepository userRepository,
                         AuthUtil authUtil,
                         @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.userRepository = userRepository;
        this.authUtil = authUtil;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            log.info("Inside JwtAuthFilter");
            log.info("Request URI: {}", request.getRequestURI());

            String path = request.getRequestURI();
            if (path.startsWith("/api/v1/public/") || path.startsWith("/api/v1/auth/")) {
                log.info("Public or Auth endpoint accessed, skipping JWT validation");
                filterChain.doFilter(request, response);
                return;
            }

            final String requestTokenHeader = request.getHeader("Authorization");
            if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
                log.warn("JWT Token does not begin with Bearer String or is missing");
                filterChain.doFilter(request, response);
                return;
            }

            log.info("Authorization header found");

            String jwtToken = requestTokenHeader.substring(7);
            String username = authUtil.getUsernameFromToken(jwtToken);

            log.info("Extracted username from token: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findByUsername(username).orElseThrow();
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                log.info("User {} authenticated successfully", username);
            }

            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            exceptionResolver.resolveException(request, response, null, ex);
        }
    }
}
