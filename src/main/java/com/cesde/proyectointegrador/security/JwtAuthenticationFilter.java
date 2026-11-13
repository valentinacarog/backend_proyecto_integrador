package com.cesde.proyectointegrador.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Claims claims = tokenProvider.getClaimsFromToken(jwt);
                String email = claims.getSubject();
                
                Object rolObj = claims.get("rol");
                if (rolObj == null) rolObj = claims.get("role");
                String rolStr = (rolObj != null) ? rolObj.toString().trim().toUpperCase() : "USUARIO";
                if (rolStr.startsWith("ROLE_")) {
                    rolStr = rolStr.substring(5);
                }

                if ("ADMINISTRADOR".equals(rolStr)) {
                    rolStr = "ADMIN";
                }

                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + rolStr));
                authorities.add(new SimpleGrantedAuthority(rolStr));

                // El Administrador Supremo recibe TODOS los roles y permisos de la plataforma
                if ("ADMIN".equals(rolStr) || "SUPERADMIN".equals(rolStr) || "admin@sistema.com".equalsIgnoreCase(email)) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_SUPERADMIN"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_EMPRESA"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_USUARIO"));
                    authorities.add(new SimpleGrantedAuthority("ADMIN"));
                    authorities.add(new SimpleGrantedAuthority("SUPERADMIN"));
                    authorities.add(new SimpleGrantedAuthority("EMPRESA"));
                    authorities.add(new SimpleGrantedAuthority("USUARIO"));
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email, null, authorities
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("No se pudo establecer la autenticación del usuario en el contexto de seguridad", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken)) {
            if (bearerToken.regionMatches(true, 0, "Bearer ", 0, 7)) {
                return bearerToken.substring(7).trim();
            }
            return bearerToken.trim();
        }
        String tokenParam = request.getHeader("x-access-token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam.trim();
        }
        return null;
    }
}
