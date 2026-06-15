package com.example.demo.Security;

import com.example.demo.Entities.User;
import com.example.demo.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CustomUserDetailsService
 *
 * Spring Security gọi loadUserByUsername() khi xác thực đăng nhập.
 * Tìm user trong DB và bọc vào CustomUserDetails.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy user: {}", username);
                    return new UsernameNotFoundException("Không tìm thấy user: " + username);
                });

        return new CustomUserDetails(user);
    }
}