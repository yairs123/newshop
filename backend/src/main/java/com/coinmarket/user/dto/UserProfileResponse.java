package com.coinmarket.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息响应")
public class UserProfileResponse {
    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "电子邮箱")
    private String email;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "显示名称")
    private String displayName;

    @Schema(description = "偏好语言")
    private String preferredLanguage;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "邮箱是否已验证")
    private boolean emailVerified;

    @Schema(description = "手机是否已验证")
    private boolean phoneVerified;

    @Schema(description = "角色列表")
    private Set<String> roles;
}
