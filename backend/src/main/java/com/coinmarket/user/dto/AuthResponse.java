package com.coinmarket.user.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String tokenType = "Bearer";
    private long expiresIn;
    private UserProfileResponse user;
}
