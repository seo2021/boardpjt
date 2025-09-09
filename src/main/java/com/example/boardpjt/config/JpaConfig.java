package com.example.boardpjt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// Audit -> 생성일시, 수정일시
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
