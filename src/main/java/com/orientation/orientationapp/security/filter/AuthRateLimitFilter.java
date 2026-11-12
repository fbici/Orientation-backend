package com.orientation.orientationapp.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/** Per-process fallback. IP comes from the transport, never client forwarded headers. */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private record Window(long start, int count) {}
    private final ConcurrentHashMap<String,Window> windows = new ConcurrentHashMap<>();
    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
            throws ServletException,IOException {
        String path=request.getServletPath();
        if (!"POST".equals(request.getMethod()) || !java.util.Set.of("/auth/login","/auth/google","/auth/register",
                "/auth/me/deletion-request","/auth/forgot-password","/auth/refresh","/auth/mobile/register","/auth/mobile/verify-email","/auth/mobile/resend-code").contains(path)) { chain.doFilter(request,response); return; }
        long now=Instant.now().getEpochSecond();
        if (windows.size()>10000) windows.entrySet().removeIf(e -> now-e.getValue().start()>=900);
        if (windows.size()>10000 && !windows.containsKey(request.getRemoteAddr())) {
            response.setStatus(429);response.setHeader("Retry-After","900");return;
        }
        Window window=windows.compute(request.getRemoteAddr(),(key,old) ->
                old==null || now-old.start()>=900 ? new Window(now,1) : new Window(old.start(),old.count()+1));
        if (window.count()>60) {
            response.setStatus(429);response.setHeader("Retry-After",Long.toString(900-(now-window.start())));
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Too many authentication requests\"}");return;
        }
        chain.doFilter(request,response);
    }
}
