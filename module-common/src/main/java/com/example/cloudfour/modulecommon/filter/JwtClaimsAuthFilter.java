package com.example.cloudfour.modulecommon.filter;


import com.example.cloudfour.modulecommon.dto.CurrentUser;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class JwtClaimsAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

//        // 헬스체크 요청은 그냥 통과하도록
//        String path = request.getRequestURI();
//        if (path != null && path.contains("/actuator/health")) {
//            // 아예 강제로 OK 리턴
//            response.setStatus(HttpServletResponse.SC_OK);
//            response.getWriter().write("{\"status\":\"UP\"}");
//            return;
//        }

        var auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                var claims = SignedJWT.parse(token).getJWTClaimsSet();
                String uid = Optional.ofNullable((String) claims.getClaim("uid"))
                        .orElseGet(claims::getSubject);
                String role = (String) claims.getClaim("role");

                Collection<? extends GrantedAuthority> authorities =
                        (role != null && !role.isBlank())
                                ? AuthorityUtils.createAuthorityList(role)
                                : AuthorityUtils.NO_AUTHORITIES;

                var principal = new CurrentUser(UUID.fromString(uid), role);

                var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ignore) { /* 토큰 없거나 못 읽으면 익명으로 진행 */ }
        }
        chain.doFilter(request, response);
    }
}
