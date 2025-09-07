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

/**
 * JWT 토큰 기반 인증을 처리하는 Spring Security 필터
 * HTTP 요청마다 한 번씩 실행되어 쿠키에서 JWT 토큰을 추출하고 유효성을 검증
 * 유효한 토큰이 있을 경우 Spring Security Context에 인증 정보를 설정
 */
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성 (의존성 주입용)
public class JwtFilter extends OncePerRequestFilter {
    // 참고: SecurityConfig에서 이 필터를 생성하고 필터 체인에 추가함

    // JWT 토큰 생성, 검증, 파싱을 담당하는 유틸리티 클래스
    private final JwtUtil jwtUtil;

    // 사용자 세부 정보를 로드하는 서비스 (DB에서 사용자 정보 조회)
    private final UserDetailsService userDetailsService;
    // 주의: 이 클래스는 Spring Bean이 아니므로 SecurityConfig에서 수동으로 의존성 주입

    /**
     * HTTP 요청마다 실행되는 필터 메인 로직
     * 쿠키에서 JWT 토큰을 추출하고, 유효한 경우 Spring Security Context에 인증 정보 설정
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 다음 필터로 요청을 전달하기 위한 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException 입출력 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // === 1단계: 쿠키에서 JWT 토큰 추출 ===
        String token = null;

        // 요청에 쿠키가 있는지 확인
        if (request.getCookies() != null) {
            // 모든 쿠키를 순회하면서 "access_token" 이름의 쿠키 찾기
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals("access_token")) {
                    token = c.getValue(); // JWT 토큰 값 추출
                    break; // 토큰을 찾았으므로 반복 종료
                }
            }
        }

        // 디버깅용 로그 출력 (운영환경에서는 제거 권장)
        System.out.println("token = " + token);

        // === 2단계: 토큰이 없는 경우 처리 ===
        if (token == null) {
            // JWT 토큰이 없는 경우 인증 없이 다음 필터로 요청 전달
            // 이 경우 SecurityConfig의 설정에 따라 접근 제한이 적용됨
            filterChain.doFilter(request, response);
            return; // 메서드 종료
        }

        // === 3단계: JWT 토큰 검증 및 인증 정보 설정 ===
        try {
            // JWT 토큰에서 사용자명(username) 추출
            // jwtUtil.getUsername()에서 토큰 유효성도 함께 검증됨
            String username = jwtUtil.getUsername(token);

            // 추출한 사용자명으로 데이터베이스에서 사용자 세부 정보 조회
            // UserDetailsService는 일반적으로 DB에서 사용자 정보와 권한을 로드
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // === Spring Security 인증 객체 생성 ===
            // UsernamePasswordAuthenticationToken (UPAT) 생성
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,                    // 주체(Principal) - 인증된 사용자 정보
                    null,                          // 자격증명(Credentials) - JWT에서는 사용하지 않음
                    userDetails.getAuthorities()   // 권한(Authorities) - 사용자 역할/권한 목록
            );

            // === Spring Security Context에 인증 정보 저장 ===
            // SecurityContextHolder에 인증 정보를 설정하면 이후 모든 Spring Security 컴포넌트에서
            // 현재 사용자가 인증되었음을 인식하고 해당 사용자 정보에 접근할 수 있음
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // JWT 토큰 검증 실패, 사용자 조회 실패 등의 예외 처리
            // 예외 발생 시 에러 로그만 출력하고 인증 없이 진행
            // (SecurityConfig 설정에 따라 접근 제한 적용됨)
            System.err.println("JWT 인증 처리 중 오류 발생: " + e.getMessage());

            // 추가 개선사항: 구체적인 예외 타입별로 다른 처리 가능
            // - ExpiredJwtException: 토큰 만료
            // - MalformedJwtException: 잘못된 토큰 형식
            // - UsernameNotFoundException: 사용자 없음
        }

        // === 4단계: 다음 필터로 요청 전달 ===
        // 인증 성공/실패와 관계없이 다음 필터로 요청을 전달
        // 이는 필터 체인의 정상적인 흐름을 보장하기 위함
        filterChain.doFilter(request, response);
    }
}