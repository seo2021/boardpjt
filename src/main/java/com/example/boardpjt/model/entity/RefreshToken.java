package com.example.boardpjt.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter // Lombok: 모든 필드에 대한 getter 메서드 자동 생성
@NoArgsConstructor // 생성자
@AllArgsConstructor // 생성자
@RedisHash(value =  "refreshToken", timeToLive = 60 * 60 * 24 * 7) // 만료: 7일
public class RefreshToken {
    @Id
    private String username;
    private String token;
}
