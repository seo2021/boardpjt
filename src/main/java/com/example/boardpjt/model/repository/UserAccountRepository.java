package com.example.boardpjt.model.repository;

import com.example.boardpjt.model.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserAccount 엔티티에 대한 데이터 접근 계층(Repository)
 * Spring Data JPA를 사용하여 데이터베이스 CRUD 작업을 처리
 * JpaRepository를 상속받아 기본적인 데이터베이스 조작 메서드들을 자동으로 제공받음
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    // JpaRepository<엔티티 클래스, Primary Key 타입>
    // - UserAccount: 관리할 엔티티 클래스
    // - Long: Primary Key(id 필드)의 데이터 타입

    // === JpaRepository에서 자동으로 제공되는 기본 메서드들 ===
    // save(UserAccount entity) - 엔티티 저장/수정
    // findById(Long id) - ID로 엔티티 조회
    // findAll() - 모든 엔티티 조회
    // deleteById(Long id) - ID로 엔티티 삭제
    // delete(UserAccount entity) - 엔티티 삭제
    // count() - 전체 레코드 수 조회
    // existsById(Long id) - ID 존재 여부 확인
    // 등등...

    /**
     * 사용자명으로 사용자 계정을 조회하는 메서드
     * Spring Data JPA의 Query Method 기능을 활용한 자동 쿼리 생성
     *
     * @param username 조회할 사용자명
     * @return Optional<UserAccount> - 조회된 사용자 계정 (존재하지 않을 수 있음)
     */
    Optional<UserAccount> findByUsername(String username);

    // === Spring Data JPA Query Method 작동 원리 ===
    // 메서드명 패턴: find + By + 엔티티필드명
    // - "findBy": 조회 작업임을 나타냄
    // - "Username": UserAccount 엔티티의 username 필드를 의미
    //
    // 자동 생성되는 SQL 쿼리:
    // SELECT * FROM user_account WHERE username = ?

    // === Optional 사용 이유 ===
    // - 사용자명이 존재하지 않을 가능성이 있음
    // - NullPointerException 방지
    // - 명시적으로 "값이 없을 수 있음"을 표현
    // - Optional.isPresent(), Optional.orElse() 등으로 안전한 처리 가능

    // === 추가 가능한 Query Method 예시 ===
    // Optional<UserAccount> findByUsernameAndRole(String username, String role);
    // List<UserAccount> findByRole(String role);
    // boolean existsByUsername(String username);
    // long countByRole(String role);
    // List<UserAccount> findByUsernameContaining(String keyword);
    // List<UserAccount> findByUsernameStartingWith(String prefix);
    // @Query("SELECT u FROM UserAccount u WHERE u.username LIKE %:keyword%")
    // List<UserAccount> searchByUsername(@Param("keyword") String keyword);

    // === Query Method 명명 규칙 ===
    // - findBy: 조회 (SELECT)
    // - countBy: 개수 (COUNT)
    // - deleteBy: 삭제 (DELETE)
    // - existsBy: 존재 여부 (EXISTS)
    //
    // 조건 연산자:
    // - And: findByUsernameAndRole
    // - Or: findByUsernameOrEmail
    // - Like: findByUsernameContaining
    // - GreaterThan: findByIdGreaterThan
    // - LessThan: findByIdLessThan
    // - Between: findByIdBetween
    // - IsNull: findByEmailIsNull
    // - IsNotNull: findByEmailIsNotNull
    // - OrderBy: findByRoleOrderByUsernameAsc
}