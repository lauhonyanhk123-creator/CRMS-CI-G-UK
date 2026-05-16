package com.crms.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String CHANGE_PASSWORD_PATH = "/api/v1/auth/change-password";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            // --- Token blacklist check (before all other logic) ---
            if (jwt != null) {
                try {
                    String[] parts = jwt.split("\\.");
                    if (parts.length == 3) {
                        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
                        Map<String, Object> claims = objectMapper.readValue(payloadJson, Map.class);
                        String tokenId = claims.containsKey("jti") ? (String) claims.get("jti") : "unknown";
                        Object expVal = claims.get("exp");
                        long expiresAt = expVal instanceof Number ? ((Number) expVal).longValue() : 0;
                        
                        if (tokenBlacklistService.isBlacklisted(tokenId)) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Token has been revoked\",\"code\":\"TOKEN_REVOKED\"}");
                            return;
                        }
                    }
                } catch (Exception e) {
                    // Don't fail the request just because we couldn't check the blacklist
                    log.warn("Could not check token blacklist: {}", e.getMessage());
                }
            }
            // --- End blacklist check ---

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // Reject TOTP challenge tokens — they are not access tokens
                if (jwtTokenProvider.isTotpChallengeToken(jwt)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"TOTP challenge token cannot be used as Bearer auth\",\"code\":\"INVALID_TOKEN_TYPE\"}");
                    return;
                }
                // Reject refresh tokens presented as Bearer auth
                if (!jwtTokenProvider.isAccessToken(jwt)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Refresh token cannot be used as Bearer auth\",\"code\":\"INVALID_TOKEN_TYPE\"}");
                    return;
                }

                String username = jwtTokenProvider.extractUsername(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtTokenProvider.isTokenValid(jwt, userDetails)) {
                    if (userDetails instanceof CustomUserDetails customUser
                            && Boolean.TRUE.equals(customUser.getUser().getMustChangePassword())
                            && !CHANGE_PASSWORD_PATH.equals(request.getRequestURI())) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write(
                                "{\"error\":\"Password change required\",\"code\":\"MUST_CHANGE_PASSWORD\"}");
                        return;
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
