package com.example.boardpjt.filter;

import com.example.boardpjt.model.entity.RefreshToken;
import com.example.boardpjt.model.repository.RefreshTokenRepository;
import com.example.boardpjt.util.CookieUtil;
import com.example.boardpjt.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성 (의존성 주입용)
public class RefreshJwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String accessToken = CookieUtil.findCookie(request, "access_token");
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }
        // Refresh Token의 경우...
        try {
            // Access Token을 검증해서... (DB랑 비교해서 쿼리를 날려본게 X)
            jwtUtil.validateToken(accessToken);
        } catch (ExpiredJwtException ex) {
            // 만료 시에는 알아서 재발급
            handleRefreshToken(request, response);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1. RefreshToken 받아오기
            String refreshToken = CookieUtil.findCookie(request, "refresh_token");
            if (refreshToken == null) {
                return;
            }

            // 2. repository에 refresh가 저장되었는지 비교 -> 검증
            String username = jwtUtil.getUsername(refreshToken); // refresh -> username
            RefreshToken stored = refreshTokenRepository.findById(username)
                    .orElseThrow(() -> new RuntimeException("Redis에 Refresh 없음"));

            if (!refreshToken.equals(stored.getToken())) {
                throw new RuntimeException("Refresh Token 불일치");
            }

            // 3. accessToken 재발급 -> cookie
            String role = jwtUtil.getRole(refreshToken);
            String newAccessToken = jwtUtil.generateToken(username, role, false);
            CookieUtil.createCookie(response, "access_token", newAccessToken, 60 * 60);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 이슈가 생기면... 내부에서 try-catch 예외 처리
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}