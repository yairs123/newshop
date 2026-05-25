package com.coinmarket.user.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank(message = "{validation.username.required}")
    @Size(min = 3, max = 50, message = "{validation.username.length}")
    private String username;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, max = 100, message = "{validation.password.length}")
    private String password;

    private String phone;
    private String phoneCountryCode;
    private String displayName;

    @NotBlank(message = "{validation.language.required}")
    private String preferredLanguage;
}
