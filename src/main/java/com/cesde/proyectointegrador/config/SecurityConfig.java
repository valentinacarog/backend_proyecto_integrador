package com.cesde.proyectointegrador.config;

import com.cesde.proyectointegrador.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Permitir peticiones preflight OPTIONS de CORS siempre
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Rutas públicas de diagnóstico, raíz y health check
                .requestMatchers("/", "/health", "/error", "/api", "/api/health").permitAll()

                // Endpoints públicos de autenticación
                .requestMatchers("/api/auth/**").permitAll()

                // Catálogo de consulta pública (lectura de retos, empresas y categorías)
                .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/categorias/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/empresas", "/api/empresas/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/retos", "/api/retos/**").permitAll()

                // ====================================================================
                // PRIVILEGIOS DEL ADMINISTRADOR SUPREMO (Panel Administrativo)
                // ====================================================================
                // Solo ADMIN puede crear y eliminar usuarios
                .requestMatchers(HttpMethod.POST, "/api/usuarios", "/api/usuarios/**").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios", "/api/usuarios/**").hasAnyRole("ADMIN", "SUPERADMIN")

                // Categorías (Crear, Editar y Eliminar sin restricciones para ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/categorias", "/api/categorias/**").hasAnyRole("ADMIN", "SUPERADMIN", "EMPRESA")
                .requestMatchers(HttpMethod.PUT, "/api/categorias", "/api/categorias/**").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/categorias", "/api/categorias/**").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categorias", "/api/categorias/**").hasAnyRole("ADMIN", "SUPERADMIN")

                // Empresas (Crear, Editar y Eliminar sin restricciones para ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/empresas", "/api/empresas/**").hasAnyRole("ADMIN", "SUPERADMIN", "EMPRESA")
                .requestMatchers(HttpMethod.PUT, "/api/empresas", "/api/empresas/**").hasAnyRole("ADMIN", "SUPERADMIN", "EMPRESA")
                .requestMatchers(HttpMethod.PATCH, "/api/empresas", "/api/empresas/**").hasAnyRole("ADMIN", "SUPERADMIN", "EMPRESA")
                .requestMatchers(HttpMethod.DELETE, "/api/empresas", "/api/empresas/**").hasAnyRole("ADMIN", "SUPERADMIN")

                // ====================================================================
                // PRIVILEGIOS DE RETOS (Empresas y Administrador)
                // ====================================================================
                .requestMatchers(HttpMethod.POST, "/api/retos", "/api/retos/**").hasAnyRole("ADMIN", "SUPERADMIN", "EMPRESA")
                .requestMatchers(HttpMethod.PUT, "/api/retos", "/api/retos/**").hasAnyRole("ADMIN", "SUPERADMIN", "EMPRESA")
                .requestMatchers(HttpMethod.PATCH, "/api/retos", "/api/retos/**").hasAnyRole("ADMIN", "SUPERADMIN", "EMPRESA")
                .requestMatchers(HttpMethod.DELETE, "/api/retos", "/api/retos/**").hasAnyRole("ADMIN", "SUPERADMIN")

                // ====================================================================
                // ACCESO PARA USUARIOS AUTENTICADOS (Estudiantes / Usuarios)
                // ====================================================================
                // Consultar usuarios y editar su propio perfil
                .requestMatchers(HttpMethod.GET, "/api/usuarios", "/api/usuarios/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/usuarios", "/api/usuarios/**").authenticated()

                // Inscripciones y seguimiento de registros en retos
                .requestMatchers("/api/registros", "/api/registros/**").authenticated()

                // Cualquier otra solicitud requiere token autenticado
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Link", "X-Total-Count"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
