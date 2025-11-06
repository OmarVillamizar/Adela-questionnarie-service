package com.example.adela.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
                
                try {
                    email = jwtUtil.extractEmail(token);
                    
                    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        if (jwtUtil.validateToken(token)) {

                            Map<String, Object> user = jwtUtil.extractUserInfo(token);
                            List<SimpleGrantedAuthority> permisos = new LinkedList<>();

                            String estado = (String) user.get("estado");
                            
                            // Determinar si es profesor basado en la presencia de estadoProfesor
                            if (user.containsKey("estadoProfesor")) {
                                // Es un profesor
                                String estadoProfesor = (String) user.get("estadoProfesor");
                                Map<String, Object> rol = (Map<String, Object>) user.get("rol");

                                if ("ACTIVA".equals(estado) && "ACTIVA".equals(estadoProfesor) && rol != null) {
                                    String rolDescripcion = (String) rol.get("descripcion");
                                    permisos.add(new SimpleGrantedAuthority("ROLE_" + rolDescripcion));
                                }
                                if ("INCOMPLETA".equals(estado)) {
                                    permisos.add(new SimpleGrantedAuthority("ROLE_PROFESOR_INCOMPLETO"));
                                }
                                if ("INACTIVA".equals(estadoProfesor)) {
                                    permisos.add(new SimpleGrantedAuthority("ROLE_PROFESOR_INACTIVO"));
                                }

                            } else {
                                // Es un estudiante
                                if ("ACTIVA".equals(estado)) {
                                    permisos.add(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"));
                                }
                                if ("INCOMPLETA".equals(estado)) {
                                    permisos.add(new SimpleGrantedAuthority("ROLE_ESTUDIANTE_INCOMPLETO"));
                                }
                            }

                            System.out.println("✅ JWT procesado correctamente para " + email);
                            System.out.println("🔹 Permisos asignados: " + permisos);

                            UsernamePasswordAuthenticationToken authenticationToken =
                                    new UsernamePasswordAuthenticationToken(email, null, permisos);

                            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        } else {
                            System.out.println("⚠️ Token no válido para: " + email);
                        }
                    }
                    
                } catch (SignatureException e) {
                    System.out.println("❌ Token JWT inválido - Firma incorrecta");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Invalid JWT signature\"}");
                    return; // IMPORTANTE: retornar sin llamar filterChain
                    
                } catch (ExpiredJwtException e) {
                    System.out.println("❌ Token JWT expirado");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"JWT token expired\"}");
                    return; // IMPORTANTE: retornar sin llamar filterChain
                    
                } catch (Exception e) {
                    System.out.println("❌ Error procesando JWT: " + e.getMessage());
                    e.printStackTrace();
                    // NO retornamos aquí - dejamos que continúe sin autenticación
                }
            } else {
                System.out.println("⚠️ No se encontró header Authorization o no es Bearer token");
            }

            // ✅ CRÍTICO: Siempre continuar la cadena de filtros
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            System.out.println("❌ Error crítico en JwtFilter: " + e.getMessage());
            e.printStackTrace();
            // Incluso en error crítico, intentamos continuar
            filterChain.doFilter(request, response);
        }
    }
}