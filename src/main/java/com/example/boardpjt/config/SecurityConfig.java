package com.example.boardpjt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // 설정 파일
@EnableWebSecurity // 시큐리티 활성화
public class SecurityConfig {
    // 1. Security Filter Chain
    @Bean // 의존성 주입에서 꺼내쓸 수 있게 컨테이너에 등록
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // http 한 번에 해도 되고 나눠서 해도...

        // 비활성화
        http.csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);
            // session 좀 나중에...

        // 보안 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/auth/register").permitAll()
                .anyRequest().authenticated()
        );

        return http.build();
    }
    // 2. PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // 3. AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
