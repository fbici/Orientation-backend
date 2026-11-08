package com.orientation.orientationapp.security.filter;

import com.orientation.orientationapp.security.jwt.JwtTokenProvider;
import com.orientation.orientationapp.modules.auth.service.impl.RefreshTokenServiceImpl;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenServiceImpl sessions;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                var claims = jwtTokenProvider.getAccessClaims(header.substring(7));
                var details = userDetailsService.loadUserByUsername(claims.getSubject());
                if (details.isEnabled() && details.isAccountNonLocked() && details.isAccountNonExpired()
                        && details.isCredentialsNonExpired()
                        && sessions.isSessionActive(UUID.fromString(claims.get("sid",String.class)),
                            UUID.fromString(claims.getSubject()))) {
                    var authentication = new UsernamePasswordAuthenticationToken(details,null,details.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (org.springframework.security.core.AuthenticationException | IllegalArgumentException | io.jsonwebtoken.JwtException ex) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request,response);
    }
}
