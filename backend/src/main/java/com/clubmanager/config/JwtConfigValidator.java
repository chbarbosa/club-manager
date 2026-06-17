package com.clubmanager.config;

import io.jsonwebtoken.io.Decoders;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtConfigValidator implements ApplicationRunner {

    public static final String DEVELOPMENT_SECRET = "Y2x1Yi1tYW5hZ2VyLWRldmVsb3BtZW50LXNlY3JldC1tdXN0LWNoYW5nZQ==";
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtConfigValidator.class);
    private static final int MIN_SECRET_BYTES = 32;

    private final JwtConfig jwtConfig;
    private final Environment environment;



    @Override
    public void run(ApplicationArguments args) {
        validate();
    }

    public void validate() {
        byte[] decoded;
        try {
            decoded = Decoders.BASE64.decode(jwtConfig.secret());
        } catch (RuntimeException exception) {
            throw new IllegalStateException("JWT secret must be valid Base64", exception);
        }
        if (decoded.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT secret must decode to at least 256 bits");
        }
        if (isProdProfile() && DEVELOPMENT_SECRET.equals(jwtConfig.secret())) {
            LOGGER.warn("Production profile cannot use the development JWT secret");
            throw new IllegalStateException("Production JWT secret must be provided with JWT_SECRET");
        }
    }

    private boolean isProdProfile() {
        return Arrays.stream(environment.getActiveProfiles()).anyMatch("prod"::equalsIgnoreCase);
    }
}
