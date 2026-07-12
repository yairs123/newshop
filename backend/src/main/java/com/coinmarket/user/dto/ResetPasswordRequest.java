package com.coinmarket.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "重置密码请求参数")
public class ResetPasswordRequest {
    @NotBlank
    @Schema(description = "重置令牌")
    private String token;

    @NotBlank
    @Size(min = 8, max = 100)
    @Schema(description = "新密码")
    private String newPassword;
}
