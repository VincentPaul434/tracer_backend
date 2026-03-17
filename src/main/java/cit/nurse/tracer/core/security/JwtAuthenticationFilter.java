package cit.nurse.tracer.core.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Extracts the submission JWT from the Authorization header on every request.
 *
 * Once validated, the submissionId (UUID) is placed as the Authentication principal
 * so controllers can inject it via @AuthenticationPrincipal UUID submissionId.
 *
 * NOTE: @Component removed — not auto-registered for the public alumni survey flow.
 * This filter can be manually wired into SecurityConfig when the admin dashboard is built.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (StringUtils.hasText(header)
            && header.startsWith(BEARER_PREFIX)
            && SecurityContextHolder.getContext().getAuthentication() == null) {

            String token = header.substring(BEARER_PREFIX.length());

            try {
                Claims claims = jwtUtils.extractClaims(token);
                applyAuthentication(claims);
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private void applyAuthentication(Claims claims) {
        Object submissionIdClaim = claims.get("submissionId");
        if (submissionIdClaim != null) {
            UUID submissionId = UUID.fromString(submissionIdClaim.toString());
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    submissionId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_SUBMISSION"))
                );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return;
        }

        Object rolesClaim = claims.get("roles");
        if (!(rolesClaim instanceof List<?> rolesRaw)) {
            return;
        }

        List<SimpleGrantedAuthority> authorities = rolesRaw.stream()
            .map(String::valueOf)
            .filter(StringUtils::hasText)
            .map(SimpleGrantedAuthority::new)
            .toList();

        if (authorities.isEmpty()) {
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);

        Object userIdClaim = claims.get("userId");
        if (userIdClaim != null) {
            authentication.setDetails(Map.of("userId", userIdClaim.toString()));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}