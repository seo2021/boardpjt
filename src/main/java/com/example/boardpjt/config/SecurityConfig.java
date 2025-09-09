package com.example.boardpjt.config;

import com.example.boardpjt.filter.JwtFilter;
import com.example.boardpjt.filter.RefreshJwtFilter;
import com.example.boardpjt.model.repository.RefreshTokenRepository;
import com.example.boardpjt.service.CustomUserDetailsService;
import com.example.boardpjt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 클래스
 * JWT 기반 인증 시스템을 구성하며, 세션을 사용하지 않는 Stateless 방식으로 동작
 */
@Configuration  // Spring의 설정 클래스임을 나타냄
@EnableWebSecurity  // Spring Security 웹 보안 활성화
@RequiredArgsConstructor  // final 필드에 대한 생성자 자동 생성 (Lombok)
public class SecurityConfig {

    // JWT 토큰 관련 유틸리티 클래스 (토큰 생성, 검증, 파싱 등)
    private final JwtUtil jwtUtil;

    // 사용자 정보를 로드하는 커스텀 서비스 (인증 시 사용자 상세정보 제공)
    private final CustomUserDetailsService userDetailsService;

    /**
     * 보안 필터 체인 설정
     * HTTP 요청에 대한 보안 규칙을 정의하고 JWT 필터를 추가
     *
     * @param http HttpSecurity 객체 - Spring Security의 HTTP 보안 설정을 위한 빌더
     * @return SecurityFilterChain - 구성된 보안 필터 체인
     * @throws Exception 설정 과정에서 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // === 기본 보안 설정 비활성화 ===
        http.csrf(AbstractHttpConfigurer::disable)  // CSRF 보호 비활성화 (REST API에서는 불필요)
                .formLogin(AbstractHttpConfigurer::disable)  // 기본 폼 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)  // HTTP Basic 인증 비활성화
                // 세션 관리 정책을 STATELESS로 설정 (JWT 사용 시 세션 불필요)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // === URL별 접근 권한 설정 ===
        http.authorizeHttpRequests(auth -> auth
                        // 홈페이지("/")와 인증 관련 경로("/auth/**")는 모든 사용자 접근 허용
                        .requestMatchers("/", "/auth/**").permitAll()
                        // auth/** -> 패턴 등록 -> auth/register 별도로 했다면, auth/logout

                        // "/my-page" 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/my-page").authenticated()

                        // ROLE_ADMIN, ROLE_USER -> (ROLE_) ...
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                // === 예외 처리 설정 ===
                .exceptionHandling(e ->
                        // 인증되지 않은 사용자가 보호된 리소스에 접근할 때 로그인 페이지로 리다이렉트
                        e.authenticationEntryPoint((req, res, ex) ->
                                res.sendRedirect("/auth/login")));

        // === JWT 필터 추가 ===
        // JwtFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
        // 모든 HTTP 요청이 JWT 필터를 먼저 거치도록 설정
        http
                .addFilterBefore(new JwtFilter(jwtUtil, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new RefreshJwtFilter(jwtUtil, userDetailsService, refreshTokenRepository), JwtFilter.class);

        // 설정이 완료된 SecurityFilterChain 반환
        return http.build();
    }

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 비밀번호 인코더 빈 등록
     * Spring Security에서 비밀번호 암호화에 사용
     *
     * @return PasswordEncoder - 위임형 패스워드 인코더 (기본적으로 BCrypt 사용)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // DelegatingPasswordEncoder 생성 - 여러 인코딩 방식을 지원하며 기본으로 BCrypt 사용
        // {bcrypt}, {noop}, {pbkdf2} 등 다양한 인코딩 방식을 자동으로 감지하고 처리
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 인증 매니저 빈 등록
     * Spring Security에서 인증 처리를 담당하는 핵심 컴포넌트
     *
     * @param configuration AuthenticationConfiguration - Spring Security 인증 설정
     * @return AuthenticationManager - 구성된 인증 매니저
     * @throws Exception 설정 과정에서 발생할 수 있는 예외
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        // AuthenticationConfiguration에서 기본 AuthenticationManager를 가져와서 반환
        // 이는 사용자 인증 (로그인) 처리 시 사용됨
        return configuration.getAuthenticationManager();
    }
}