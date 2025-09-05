package com.example.boardpjt.filter;

import com.example.boardpjt.util.JwtUtil;
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

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    // SecurityConfig

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    // 스프링으로 관리하진 않을텐데 -> SecurityConfig에서 주입할 예정

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals("access_token")) {
                    token = c.getValue();
                    break;
                }
            }
        }
        if (token == null) {
            filterChain.doFilter(request, response);
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
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // 나중에 꼬이지 않게 미리...
        filterChain.doFilter(request, response);
    }


}
