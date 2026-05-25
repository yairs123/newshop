package com.coinmarket.user.dto;

import lombok.*;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String displayName;
    private String preferredLanguage;
    private String avatarUrl;
    private boolean emailVerified;
    private boolean phoneVerified;
    private Set<String> roles;
}
