package com.example.chatservice.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = resolveToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                authenticateUser(token, request);
            }
        } catch (ExpiredJwtException e) {
            request.setAttribute("expired", true);
        } catch ( JwtException | IllegalArgumentException e) {
            request.setAttribute("invalid", true);
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).trim();
        }
        return null;
    }

    private void authenticateUser(String token, HttpServletRequest request) {
        try {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            List<String> roles = jwtTokenProvider.getRolesFromToken(token);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null,
                            roles.stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toList())
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
//            log.info("Token from header: {}", token); debug
//            log.info("Authenticated user: {}", username); debug
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }
    }
}