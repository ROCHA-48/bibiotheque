package com.ibizabroker.bibliotheque.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        String reason = (String) request.getAttribute(JwtRequestFilter.AUTH_FAILURE_ATTRIBUTE);
        if (reason == null) {
            reason = "MISSING";
        }

        String message;
        switch (reason) {
            case "EXPIRED":
                message = "Votre session a expiré. Veuillez vous reconnecter.";
                break;
            case "INVALID":
                message = "Token invalide. Authentification refusée.";
                break;
            default:
                message = "Authentification requise. Veuillez fournir un token valide.";
                break;
        }

        logger.info("Accès refusé (401) sur {} {} — cause : {}", request.getMethod(), request.getRequestURI(), reason);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, String> body = new HashMap<>();
        body.put("status", String.valueOf(HttpStatus.UNAUTHORIZED.value()));
        body.put("error", "Unauthorized");
        body.put("message", message);
        body.put("reason", reason);

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }

}
