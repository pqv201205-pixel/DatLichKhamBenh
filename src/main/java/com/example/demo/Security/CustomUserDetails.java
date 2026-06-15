package com.example.demo.Security;

import com.example.demo.Entities.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * CustomUserDetails
 *
 * Bọc User entity thành UserDetails để Spring Security sử dụng.
 * Lưu thêm userId và role để lấy nhanh trong Controller qua @AuthenticationPrincipal.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Integer userId;
    private final String  username;
    private final String  password;
    private final Enum  role;
    private final boolean isActive;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.userId     = user.getUserId();
        this.username   = user.getUsername();
        this.password   = user.getPasswordHash();
        this.role       = user.getRole();
        this.isActive   = Boolean.TRUE.equals(user.getIsActive());
        // ROLE_ prefix bắt buộc cho Spring Security hasRole()
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override public String getPassword()                       { return password; }
    @Override public String getUsername()                       { return username; }
    @Override public boolean isAccountNonExpired()              { return true; }
    @Override public boolean isAccountNonLocked()               { return isActive; }
    @Override public boolean isCredentialsNonExpired()          { return true; }
    @Override public boolean isEnabled()                        { return isActive; }
}