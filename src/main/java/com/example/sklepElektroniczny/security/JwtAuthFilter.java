package com.example.sklepElektroniczny.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    private static final Logger logFilter = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain)
            throws ServletException, IOException {

        String path = httpRequest.getRequestURI();

        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-resources") || path.startsWith("/webjars") || path.startsWith("/api/docs")) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }

        logFilter.debug("JwtAuthFilter triggered for URI: {}", path);

        try {
            String token = extractJwt(httpRequest);
            if (token != null && jwtTokenUtil.validateJwtToken(token)) {
                String extractedUsername = jwtTokenUtil.getUserNameFromJwtToken(token);

                UserDetails loadedUser = customUserDetailsService.loadUserByUsername(extractedUsername);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                loadedUser,
                                null,
                                loadedUser.getAuthorities());

                logFilter.debug("Extracted roles from JWT: {}", loadedUser.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception ex) {
            logFilter.error("Failed to set user authentication: {}", ex.getMessage());
        }

        chain.doFilter(httpRequest, httpResponse);
    }

    private String extractJwt(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return jwtTokenUtil.getJwtFromCookies(request);
    }

}


