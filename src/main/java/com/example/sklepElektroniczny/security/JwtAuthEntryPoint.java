package com.example.sklepElektroniczny.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logEntry = LoggerFactory.getLogger(JwtAuthEntryPoint.class);

    @Override
    public void commence(HttpServletRequest httpRequest, HttpServletResponse httpResponse, AuthenticationException authEx)
            throws IOException, ServletException {
        logEntry.warn("Access denied: {}", authEx.getMessage());

        httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> responsePayload = new LinkedHashMap<>();
        responsePayload.put("statusCode", HttpServletResponse.SC_UNAUTHORIZED);
        responsePayload.put("errorType", "Access Denied");
        responsePayload.put("errorMessage", authEx.getMessage());
        responsePayload.put("requestPath", httpRequest.getRequestURI());

        ObjectMapper jsonProcessor = new ObjectMapper();
        jsonProcessor.writeValue(httpResponse.getOutputStream(), responsePayload);
    }
}
