package com.coinmarket.user.repository;

import com.coinmarket.user.entity.Role;
import com.coinmarket.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User Repository Integration Test")
class UserRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("保存用户并通过用户名查找")
    void saveAndFindByUsername() {
        Role buyerRole = roleRepository.findByName("ROLE_BUYER").orElse(null);

        User user = User.builder()
                .username("testuser")
                .email("testuser@test.com")
                .passwordHash("encoded-password")
                .displayName("Test User")
                .preferredLanguage("en")
                .enabled(true)
                .roles(buyerRole != null ? Set.of(buyerRole) : Set.of())
                .build();

        User saved = userRepository.save(user);
        assertThat(saved.getId()).isNotNull();

        Optional<User> found = userRepository.findByUsername("testuser");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("testuser@test.com");
        assertThat(found.get().isEnabled()).isTrue();
    }

    @Test
    @DisplayName("保存用户并通过邮箱查找")
    void saveAndFindByEmail() {
        Role buyerRole = roleRepository.findByName("ROLE_BUYER").orElse(null);
        Set<Role> roles = buyerRole != null ? Set.of(buyerRole) : Set.of();

        User user = User.builder()
                .username("emailuser")
                .email("findme@test.com")
                .passwordHash("hash123")
                .displayName("Email User")
                .preferredLanguage("en")
                .enabled(true)
                .roles(roles)
                .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("findme@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("emailuser");
    }

    @Test
    @DisplayName("检查用户名是否存在")
    void existsByUsername() {
        Role buyerRole = roleRepository.findByName("ROLE_BUYER").orElse(null);
        Set<Role> roles = buyerRole != null ? Set.of(buyerRole) : Set.of();

        User user = User.builder()
                .username("uniqueuser")
                .email("unique@test.com")
                .passwordHash("hash456")
                .displayName("Unique User")
                .preferredLanguage("en")
                .enabled(true)
                .roles(roles)
                .build();
        userRepository.save(user);

        assertThat(userRepository.existsByUsername("uniqueuser")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("不存在的用户返回空")
    void unknownUser_returnsEmpty() {
        Optional<User> found = userRepository.findByUsername("nobody");
        assertThat(found).isEmpty();

        Optional<User> byEmail = userRepository.findByEmail("nobody@test.com");
        assertThat(byEmail).isEmpty();

        Optional<User> byId = userRepository.findById(999L);
        assertThat(byId).isEmpty();
    }
}
