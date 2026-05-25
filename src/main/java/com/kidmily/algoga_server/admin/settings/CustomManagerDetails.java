package com.kidmily.algoga_server.admin.settings;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

public class CustomManagerDetails implements UserDetails {

    private final Long id;          // 매니저 PK
    private final String loginId;   // 로그인 아이디
    private final String role;      // ROLE_CONTENT_MANAGER 등

    public CustomManagerDetails(Long id, String loginId, String role) {
        this.id = id;
        this.loginId = loginId;
        this.role = role;
    }

    // 🌟 컨트롤러에서 꺼내 쓸 게터 메서드
    public Long getId() { return id; }
    public String getRole() { return role; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 토큰에 "SUPER_ADMIN"으로 들어있든 "ROLE_SUPER_ADMIN"으로 들어있든
        // 무조건 "ROLE_" 접두사가 붙도록 안전장치를 추가합니다.
        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        return Collections.singletonList(new SimpleGrantedAuthority(authority));
    }

    @Override public String getPassword() { return null; } // 비밀번호는 토큰 인증에 불필요하므로 null
    @Override public String getUsername() { return loginId; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}