package rs.ac.uns.acs.nais.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if ("/api/auth/login".equals(request.getServletPath())) {
            log.info("Auth login request: method={}, servletPath={}", request.getMethod(), request.getServletPath());
        }
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return false;
        }
        return "/api/auth/login".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null) {
            header = header.trim();
        }
        if (header == null || header.length() < 8 || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7).trim();
        try {
            var claims = jwtService.parseClaims(token);
            String sub = claims.getSubject();
            Object roleClaim = claims.get("role");
            String role = null;
            if (roleClaim instanceof String s && !s.isBlank()) {
                role = s.trim();
            } else if (roleClaim != null) {
                role = String.valueOf(roleClaim).trim();
            }
            if (role == null || role.isBlank()) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }
            long korisnikId = Long.parseLong(sub);
            var auth = new UsernamePasswordAuthenticationToken(
                    korisnikId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
