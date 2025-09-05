package com.example.boardpjt.model.repository;

import com.example.boardpjt.model.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    // findByUsername
    // JPA -> entity -> 속성 -> 메서드
    Optional<UserAccount> findByUsername(String username);
}
