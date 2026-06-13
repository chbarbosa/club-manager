package com.clubmanager.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class JwtConfigValidatorTest {

    @Test
    void validate_WithValidSecret_AllowsStartup() {
        JwtConfigValidator validator = new JwtConfigValidator(
                new JwtConfig("MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=", 86400000),
                new MockEnvironment());

        assertThatCode(validator::validate).doesNotThrowAnyException();
    }

    @Test
    void validate_WithInvalidBase64_ThrowsException() {
        JwtConfigValidator validator = new JwtConfigValidator(
                new JwtConfig("not-base64", 86400000),
                new MockEnvironment());

        assertThatThrownBy(validator::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("valid Base64");
    }

    @Test
    void validate_WithShortSecret_ThrowsException() {
        JwtConfigValidator validator = new JwtConfigValidator(
                new JwtConfig("c2hvcnQ=", 86400000),
                new MockEnvironment());

        assertThatThrownBy(validator::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("256 bits");
    }

    @Test
    void validate_WithProdProfileAndDevelopmentSecret_ThrowsException() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        JwtConfigValidator validator = new JwtConfigValidator(
                new JwtConfig(JwtConfigValidator.DEVELOPMENT_SECRET, 86400000),
                environment);

        assertThatThrownBy(validator::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Production JWT secret");
    }
}
