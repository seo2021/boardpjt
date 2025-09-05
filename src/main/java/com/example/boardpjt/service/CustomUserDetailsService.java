package com.example.boardpjt.service;

import com.example.boardpjt.model.entity.UserAccount;
import com.example.boardpjt.model.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // 의존성 주입
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    // jwt -> username -> 전달
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Repository -> userAccount -> 없을 때 UsernameNotFoundEx.
        UserAccount userAccount = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
        // User(UserDetails) Spring Security. <- UserAccount.
        return User.builder()
                .username(userAccount.getUsername())
                .password(userAccount.getPassword())
                // ROLE_*** -> User -> 붙어있으면 X
                .roles(userAccount.getRole().replace("ROLE_", ""))
                .build();
    }
}
