package com.example.boardpjt.filter;

import com.example.boardpjt.model.entity.RefreshToken;
import com.example.boardpjt.model.repository.RefreshTokenRepository;
import com.example.boardpjt.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class RefreshTokenReissueFilter extends OncePerRequestFilter {
    // SecurityConfig

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                // access_token?
                if (c.getName().equals("access_token")) {
                    token = c.getValue();
                    break;
                }
            }
        }
        System.out.println("token = " + token);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtUtil.getUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            Authentication authentication =
                    // UPAT
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
            // token의 Username으로 찾아낸 인증 정보를 SecurityContextHolder에 주입
            SecurityContextHolder.getContext().setAuthentication(authentication);
        // 만료 에러
        } catch (ExpiredJwtException e) {
            System.out.println("!!!토큰 만료 감지!!!");
            System.out.println("Refresh 토큰 검사");
            String refreshToken = null;
            if (request.getCookies() != null) {
                for (Cookie c : request.getCookies()) {
                    // refresh_token?
                    if (c.getName().equals("refresh_token")) {
                        refreshToken = c.getValue();
                        break;
                    }
                }
            }
            if (refreshToken != null) {
                try {
                    String username = jwtUtil.getUsername(refreshToken);
                    RefreshToken stored = refreshTokenRepository.findById(username).orElseThrow(() -> new RuntimeException("Redis에 해당 토큰 없음"));
                    if (stored.getToken().equals(refreshToken)) {
                        System.out.println("검증 성공!");
                        ResponseCookie newAccessCookie = ResponseCookie.from("access_token").httpOnly(true).path("/").maxAge(3600).build();
                        response.addHeader(HttpHeaders.SET_COOKIE, newAccessCookie.toString());
                    }
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    Authentication authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );
                    // token의 Username으로 찾아낸 인증 정보를 SecurityContextHolder에 주입
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception ex) {
                    System.out.println("Refresh 토큰이 잘못됌!");
                }
            }
        // 포맷이나 일반 에러...
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        // 나중에 꼬이지 않게 미리...
        filterChain.doFilter(request, response);
    }
}
