package com.coinmarket.common.security;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private Long id;
    private String username;
    private Collection<? extends GrantedAuthority> authorities;
}
