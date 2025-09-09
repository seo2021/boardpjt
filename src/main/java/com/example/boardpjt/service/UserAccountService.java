package com.example.boardpjt.service;

import com.example.boardpjt.model.entity.UserAccount;
import com.example.boardpjt.model.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 계정 관련 비즈니스 로직을 처리하는 서비스 클래스
 * 회원가입, 사용자 정보 관리 등의 핵심 기능을 담당
 * 데이터베이스 트랜잭션과 비밀번호 암호화를 포함한 안전한 사용자 등록 처리
 */
@Service // Spring의 서비스 빈으로 등록 (비즈니스 로직을 담당하는 @Component의 특화 버전)
@RequiredArgsConstructor // final로 선언된 필드들에 대한 생성자 자동 생성 (의존성 주입용)
// 생성자 주입 방식: 필드 주입(@Autowired)보다 권장되는 방식
// - 불변성 보장 (final 키워드 사용 가능)
// - 순환 참조 방지
// - 테스트 용이성 향상
public class UserAccountService {

    // 사용자 계정 데이터를 데이터베이스에서 조회/저장하기 위한 Repository
    private final UserAccountRepository userAccountRepository;

    // 비밀번호 암호화를 위한 Spring Security의 PasswordEncoder
    // SecurityConfig에서 BCrypt 기반의 DelegatingPasswordEncoder로 설정됨
    private final PasswordEncoder passwordEncoder;

    /**
     * 새로운 사용자를 등록하는 메서드
     * 사용자명 중복 검사, 비밀번호 암호화, 기본 권한 설정을 포함한 완전한 회원가입 처리
     *
     * @param username 등록할 사용자명 (중복 불가)
     * @param password 사용자가 입력한 평문 비밀번호 (암호화되어 저장됨)
     * @return UserAccount 생성된 사용자 계정 엔티티 (id 포함)
     * @throws IllegalArgumentException 이미 존재하는 사용자명일 경우 발생
     */
    @Transactional // 데이터베이스 트랜잭션 관리 - 메서드 전체가 하나의 트랜잭션으로 처리됨
    // @Transactional의 역할:
    // - 메서드 실행 중 예외 발생 시 모든 변경사항 롤백
    // - 메서드 정상 완료 시 모든 변경사항 커밋
    // - 데이터 일관성 보장
    // - 기본 전파 수준: REQUIRED (기존 트랜잭션이 있으면 참여, 없으면 새로 생성)
    public UserAccount register(String username, String password) {

        // === 1단계: 사용자명 중복 검사 ===
        // findByUsername()은 Optional<UserAccount>를 반환
        // isPresent(): Optional에 값이 있는지 확인 (즉, 해당 사용자명이 이미 존재하는지 검사)
        if (userAccountRepository.findByUsername(username).isPresent()) {
            // 중복된 사용자명이 존재할 경우 예외 발생
            // 이 예외는 Controller에서 catch되어 사용자에게 에러 메시지로 표시됨
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다: " + username);
        }

        // === 2단계: 새로운 UserAccount 엔티티 생성 및 설정 ===
        UserAccount userAccount = new UserAccount();

        // 사용자명 설정 (입력받은 값 그대로 저장)
        userAccount.setUsername(username);

        // === 비밀번호 암호화 및 설정 ===
        // passwordEncoder.encode(): 평문 비밀번호를 암호화
        // - BCrypt 알고리즘 사용 (기본 설정)
        // - Salt 자동 생성으로 같은 비밀번호라도 다른 해시값 생성
        // - 단방향 암호화로 복호화 불가능 (보안성 확보)
        // 예: "password123" → "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKWKQp1bR..."
        userAccount.setPassword(passwordEncoder.encode(password));

        // === 기본 권한 설정 ===
        // 새로 가입하는 모든 사용자에게 "ROLE_USER" 권한 부여
        // Spring Security에서 권한은 "ROLE_" 접두사를 사용하는 것이 관례
        // - ROLE_USER: 일반 사용자 권한
        // - ROLE_ADMIN: 관리자 권한 (별도 로직으로 부여)
        userAccount.setRole("ROLE_USER");

        // === 3단계: 데이터베이스에 저장 ===
        // save() 메서드는 JPA의 persist 작동 방식:
        // - id가 null인 경우: INSERT 쿼리 실행 (새로운 엔티티 생성)
        // - id가 존재하는 경우: UPDATE 쿼리 실행 (기존 엔티티 수정)
        // 저장 후 자동 생성된 id가 포함된 UserAccount 객체 반환
        return userAccountRepository.save(userAccount);

        // === 트랜잭션 커밋 ===
        // 메서드가 정상적으로 종료되면 @Transactional에 의해 자동 커밋
        // 예외 발생 시 자동 롤백되어 데이터 일관성 보장
    }

    // === 추가 구현 고려사항 ===
    // 1. 비밀번호 정책 검증 메서드:
    //    private void validatePassword(String password) {
    //        if (password.length() < 8) throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다");
    //    }
    //
    // 2. 이메일 인증 기능:
    //    public void sendVerificationEmail(String email) { ... }
    //
    // 3. 사용자 정보 수정 메서드:
    //    @Transactional
    //    public UserAccount updateUserInfo(Long id, String newPassword) { ... }
    //
    // 4. 사용자 삭제 메서드:
    //    @Transactional
    //    public void deleteUser(Long id) { ... }
    //
    // 5. 비밀번호 변경 메서드:
    //    @Transactional
    //    public void changePassword(String username, String oldPassword, String newPassword) { ... }

    // 유저 전체를 조회하는 메서드
    @Transactional(readOnly = true)
    public List<UserAccount> findAllUsers() {
        return userAccountRepository.findAll();
    }

    // 유저를 탈퇴(삭제) 메서드
    @Transactional
    public void deleteUser(Long id) {
        userAccountRepository.deleteById(id);
    }

    public UserAccount findByUsername(String name) {
        return userAccountRepository.findByUsername(name)
                .orElseThrow();
    }
}