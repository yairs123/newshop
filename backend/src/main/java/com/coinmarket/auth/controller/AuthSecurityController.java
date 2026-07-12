package com.coinmarket.auth.controller;

import com.coinmarket.common.dto.ApiResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@Tag(name = "安全认证", description = "验证码、2FA双因素认证等安全相关接口")
public class AuthSecurityController {

    private final StringRedisTemplate redisTemplate;
    private final SecretKey jwtSecret;

    private static final Random RANDOM = new Random();

    public AuthSecurityController(StringRedisTemplate redisTemplate,
                                   @Value("${app.jwt.secret}") String jwtSecret) {
        this.redisTemplate = redisTemplate;
        this.jwtSecret = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/captcha")
    @Operation(summary = "获取验证码", description = "获取数学验证码题目和token，用户需计算结果后提交验证")
    public ApiResponse<Map<String, String>> getCaptcha() {
        int a = RANDOM.nextInt(10, 99);
        int b = RANDOM.nextInt(1, 50);
        int op = RANDOM.nextInt(2); // 0=+, 1=-
        String question;
        int answer;
        if (op == 0) {
            question = a + " + " + b + " = ?";
            answer = a + b;
        } else {
            question = a + " - " + b + " = ?";
            answer = a - b;
        }

        // 用JWT签名验证码答案，防止篡改
        long now = System.currentTimeMillis();
        String token = Jwts.builder()
                .claim("captcha", answer)
                .issuedAt(new Date(now))
                .expiration(new Date(now + 300_000)) // 5分钟有效
                .signWith(jwtSecret)
                .compact();

        return ApiResponse.success(Map.of(
                "question", question,
                "token", token
        ));
    }

    @PostMapping("/send-2fa")
    @Operation(summary = "发送2FA验证码", description = "密码验证通过后，发送双因素验证码到用户邮箱")
    public ApiResponse<Map<String, String>> send2faCode(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank()) {
            return ApiResponse.error("用户名不能为空");
        }

        // 生成6位验证码
        String code = String.format("%06d", RANDOM.nextInt(1000000));
        String redisKey = "2fa:" + username;

        // 存入Redis，5分钟有效
        redisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);

        // 生成一个一次性token用于提交验证码
        long now = System.currentTimeMillis();
        String verifyToken = Jwts.builder()
                .claim("username", username)
                .claim("purpose", "2fa")
                .issuedAt(new Date(now))
                .expiration(new Date(now + 300_000))
                .signWith(jwtSecret)
                .compact();

        // 实际项目中这里应该发邮件/短信
        // 开发环境把验证码输出到日志方便测试
        log.info("2FA code for {}: {}", username, code);
        return ApiResponse.success(Map.of(
                "verifyToken", verifyToken,
                "message", "验证码已发送到您的邮箱"
        ));
    }

    @PostMapping("/verify-2fa")
    @Operation(summary = "验证2FA码", description = "提交双因素验证码完成二次验证")
    public ApiResponse<Map<String, Object>> verify2fa(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String verifyToken = body.get("verifyToken");

        if (code == null || verifyToken == null) {
            return ApiResponse.error("参数不全");
        }

        // 解析token获取用户名
        String username;
        try {
            var claims = Jwts.parser()
                    .verifyWith(jwtSecret)
                    .build()
                    .parseSignedClaims(verifyToken)
                    .getPayload();
            username = claims.get("username", String.class);
        } catch (Exception e) {
            return ApiResponse.error("验证码已过期，请重新获取");
        }

        // 检查Redis中的验证码
        String redisKey = "2fa:" + username;
        String savedCode = redisTemplate.opsForValue().get(redisKey);
        if (savedCode == null) {
            return ApiResponse.error("验证码已过期，请重新获取");
        }

        if (!savedCode.equals(code)) {
            return ApiResponse.error("验证码错误");
        }

        // 验证通过，删除验证码
        redisTemplate.delete(redisKey);

        return ApiResponse.success(Map.of(
                "verified", true,
                "username", username
        ));
    }
}
