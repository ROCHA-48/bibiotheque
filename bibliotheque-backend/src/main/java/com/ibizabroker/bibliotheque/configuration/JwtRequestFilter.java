package com.ibizabroker.bibliotheque.configuration;

import com.ibizabroker.bibliotheque.service.JwtService;
import com.ibizabroker.bibliotheque.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    /** Attribut de requête transportant la cause de l'échec d'authentification. */
    public static final String AUTH_FAILURE_ATTRIBUTE = "jwtAuthFailureReason";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
            } catch (ExpiredJwtException e) {
                logger.info("Tentative avec un token expiré sur {} {}", request.getMethod(), request.getRequestURI());
                request.setAttribute(AUTH_FAILURE_ATTRIBUTE, "EXPIRED");
            } catch (io.jsonwebtoken.MalformedJwtException e) {
                logger.info("Token JWT mal formé sur {} {}", request.getMethod(), request.getRequestURI());
                request.setAttribute(AUTH_FAILURE_ATTRIBUTE, "INVALID");
            } catch (io.jsonwebtoken.SignatureException e) {
                logger.info("Signature JWT invalide sur {} {}", request.getMethod(), request.getRequestURI());
                request.setAttribute(AUTH_FAILURE_ATTRIBUTE, "INVALID");
            } catch (IllegalArgumentException e) {
                logger.info("Token JWT illisible sur {} {}", request.getMethod(), request.getRequestURI());
                request.setAttribute(AUTH_FAILURE_ATTRIBUTE, "INVALID");
            } catch (io.jsonwebtoken.JwtException e) {
                logger.info("Token JWT rejeté sur {} {}", request.getMethod(), request.getRequestURI());
                request.setAttribute(AUTH_FAILURE_ATTRIBUTE, "INVALID");
            }
        } else if (requestTokenHeader == null) {
            request.setAttribute(AUTH_FAILURE_ATTRIBUTE, "MISSING");
        } else {
            logger.info("En-tête Authorization mal formé sur {} {}", request.getMethod(), request.getRequestURI());
            request.setAttribute(AUTH_FAILURE_ATTRIBUTE, "INVALID");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = jwtService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwtToken, userDetails)) {

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            } else {
                request.setAttribute(AUTH_FAILURE_ATTRIBUTE, "INVALID");
            }
        }
        filterChain.doFilter(request, response);

    }

}
