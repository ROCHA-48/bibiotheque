package com.ibizabroker.bibliotheque.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Journalise les tentatives d'accès refusées (RS-02, RS-03) et renvoie un 403 JSON.
 * Appelé uniquement pour des utilisateurs authentifiés (sinon c'est l'entry point qui répond 401).
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(JwtAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {

        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication() != null
                ? org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName()
                : "inconnu";

        logger.warn("Accès refusé (403) : utilisateur '{}' sur {} {} — {}",
                username, request.getMethod(), request.getRequestURI(), accessDeniedException.getMessage());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, String> body = new HashMap<>();
        body.put("status", String.valueOf(HttpStatus.FORBIDDEN.value()));
        body.put("error", "Forbidden");
        body.put("message", "Accès interdit : vous n'avez pas les droits nécessaires pour cette action.");

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }

}
