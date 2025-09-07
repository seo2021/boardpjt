package com.example.boardpjt.controller;

import com.example.boardpjt.service.UserAccountService;
import com.example.boardpjt.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 사용자 인증 관련 컨트롤러
 * 회원가입, 로그인 기능을 담당하며 HTML 뷰를 반환하는 전통적인 MVC 컨트롤러
 * JWT 토큰을 HTTP 쿠키를 통해 관리
 */
@Controller // Spring MVC 컨트롤러로 등록 (view 반환 → template → html 렌더링, thymeleaf 사용)
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성 (의존성 주입용)
@RequestMapping("/auth") // 모든 요청 경로에 "/auth" 접두사 추가 (/auth/**)
public class AuthController {

    // 사용자 계정 관련 비즈니스 로직을 처리하는 서비스
    private final UserAccountService userAccountService;

    // Spring Security의 인증 매니저 (사용자 로그인 인증 처리)
    private final AuthenticationManager authenticationManager;

    // JWT 토큰 생성, 검증, 파싱을 담당하는 유틸리티 클래스
    private final JwtUtil jwtUtil;

    /**
     * 회원가입 페이지를 보여주는 메서드
     * GET 요청으로 회원가입 폼을 사용자에게 제공
     *
     * @return String - 렌더링할 템플릿 파일명 (templates/register.html)
     */
    @GetMapping("/register") // GET /auth/register 요청 처리
    public String registerForm() {
        // Thymeleaf 템플릿 엔진이 templates/register.html 파일을 찾아서 렌더링
        return "register";
    }

    /**
     * 회원가입 처리 메서드
     * POST 요청으로 전송된 사용자 정보를 받아서 회원가입을 처리
     *
     * @param username 사용자가 입력한 사용자명
     * @param password 사용자가 입력한 비밀번호
     * @param redirectAttributes 리다이렉트 시 데이터 전달을 위한 객체
     * @return String - 리다이렉트할 경로
     */
    @PostMapping("/register") // POST /auth/register 요청 처리
    public String register(@RequestParam String username, // HTML 폼의 username 파라미터
                           @RequestParam String password, // HTML 폼의 password 파라미터
                           RedirectAttributes redirectAttributes) { // 리다이렉트 시 속성 전달

        // TODO: @Valid 어노테이션으로 유효성 검증 추가 가능 (Bean Validation)
        try {
            // UserAccountService를 통해 회원가입 처리 (사용자 저장, 비밀번호 암호화 등)
            userAccountService.register(username, password);

            // 회원가입 성공 시 홈페이지로 리다이렉트
            return "redirect:/";

        } catch (IllegalArgumentException e) {
            // 회원가입 실패 (예: 중복 사용자명) 시 예외 처리

            // addAttribute vs addFlashAttribute 차이점:
            // addAttribute: URL 파라미터로 전달 (?error=message)
            // addFlashAttribute: 세션에 임시 저장 후 한 번만 사용되고 자동 제거
            redirectAttributes.addFlashAttribute("error", e.getMessage());

            // 에러 메시지와 함께 회원가입 페이지로 다시 리다이렉트
            return "redirect:/auth/register";
        }
    }

    /**
     * 로그인 페이지를 보여주는 메서드
     * GET 요청으로 로그인 폼을 사용자에게 제공
     *
     * @return String - 렌더링할 템플릿 파일명 (templates/login.html)
     */
    @GetMapping("/login") // GET /auth/login 요청 처리
    public String loginForm() {
        // Thymeleaf 템플릿 엔진이 templates/login.html 파일을 찾아서 렌더링
        return "login";
    }

    /**
     * 로그인 처리 메서드
     * POST 요청으로 전송된 로그인 정보를 받아서 인증을 처리하고 JWT 토큰을 발급
     *
     * @param username 사용자가 입력한 사용자명
     * @param password 사용자가 입력한 비밀번호
     * @param response HTTP 응답 객체 (쿠키 설정을 위해 사용)
     * @param redirectAttributes 리다이렉트 시 데이터 전달을 위한 객체
     * @return String - 리다이렉트할 경로
     */
    @PostMapping("/login") // POST /auth/login 요청 처리
    public String login(@RequestParam String username, // HTML 폼의 username 파라미터
                        @RequestParam String password, // HTML 폼의 password 파라미터
                        HttpServletResponse response, // HTTP 응답 객체 (쿠키 설정용)
                        RedirectAttributes redirectAttributes) { // 리다이렉트 시 속성 전달
        try {
            // === 사용자 인증 단계 ===
            // Spring Security의 AuthenticationManager를 통해 사용자 인증 시도
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            username, password // 사용자명과 비밀번호로 인증 토큰 생성
                    ));
            // 인증 성공 시 Authentication 객체에 사용자 정보와 권한이 포함됨

            // === JWT 토큰 발급 단계 ===
            // 인증된 사용자 정보를 바탕으로 JWT Access Token 생성
            String accessToken = jwtUtil.generateToken(
                    username, // 토큰 subject (사용자명)
                    authentication.getAuthorities().toString(), // 사용자 권한 정보
                    false // Access Token 타입 (Refresh Token이 아님)
            );

            // === HTTP 쿠키로 토큰 저장 단계 ===
            // JWT 토큰을 HTTP 쿠키로 생성 (보안 설정 포함)
            ResponseCookie cookie = ResponseCookie.from("access_token", accessToken)
                    .httpOnly(true) // XSS 공격 방지 (JavaScript에서 접근 불가)
                    .path("/") // 쿠키가 유효한 경로 (전체 도메인)
                    .maxAge(3600) // 쿠키 유효기간 (3600초 = 1시간) 주의: 초 단위임
                    .build();

            // HTTP 응답 헤더에 "Set-Cookie" 추가하여 클라이언트에 쿠키 전송
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // === 로그인 성공 후 리다이렉트 ===
            // 인증 완료 후 마이페이지로 이동
            return "redirect:/my-page";

        } catch (Exception e) {
            // === 로그인 실패 처리 ===
            // 인증 실패 시 (잘못된 사용자명/비밀번호, 계정 비활성화 등)
            redirectAttributes.addFlashAttribute("error", "로그인 실패");

            // 에러 메시지와 함께 로그인 페이지로 다시 리다이렉트
            return "redirect:/auth/login";
        }
    }
}