package com.projects.crm.configuration.filter;

import com.projects.crm.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        // Проверяем наличие и правильный формат заголовка
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Извлекаем JWT из заголовка
        final String jwt = authorizationHeader.substring(7);
        final String userEmail = jwtService.getEmailFromToken(jwt);

        // Проверяем, что пользователь ещё не аутентифицирован
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // Проверяем валидность токена
            if (jwtService.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                // Устанавливаем детали запроса
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Устанавливаем аутентификацию в SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Продолжаем цепочку фильтров
        filterChain.doFilter(request, response);
    }
}

