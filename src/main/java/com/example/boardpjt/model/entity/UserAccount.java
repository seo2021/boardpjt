package com.example.boardpjt.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
// 기본 생성자
// @Table -> 필수는 아님. 테이블명을 바꿔주고 싶다던가...
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MYSQL의 AUTO_INCREMENT
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    @Column(nullable = false) // PasswordEncoder -> 어차피 저장되는 형태는 암호화된 상태
    private String password;
    @Column(nullable = false, length = 20)
    private String role; // ROLE_USER, ROLE_ADMIN
}
