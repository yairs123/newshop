package com.coinmarket.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "注册请求参数")
public class RegisterRequest {
    @NotBlank(message = "{validation.username.required}")
    @Size(min = 3, max = 50, message = "{validation.username.length}")
    @Schema(description = "用户名")
    private String username;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    @Schema(description = "电子邮箱")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, max = 100, message = "{validation.password.length}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "{validation.password.complexity}")
    @Schema(description = "密码（需包含大小写字母和数字）")
    private String password;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "手机国家码")
    private String phoneCountryCode;

    @Schema(description = "显示名称")
    private String displayName;

    @NotBlank(message = "{validation.language.required}")
    @Schema(description = "偏好语言")
    private String preferredLanguage;
}
