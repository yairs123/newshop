package com.coinmarket.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "登录请求参数")
public class LoginRequest {
    @NotBlank
    @Schema(description = "用户名")
    private String username;

    @NotBlank
    @Schema(description = "密码")
    private String password;

    @Schema(description = "验证码Token（前端获取验证码时下发）")
    private String captchaToken;

    @Schema(description = "验证码答案")
    private String captchaAnswer;

    @Schema(description = "双因素认证验证码")
    private String twoFactorCode;
}
