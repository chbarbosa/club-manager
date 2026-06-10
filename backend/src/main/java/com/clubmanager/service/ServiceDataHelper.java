package com.clubmanager.service;

import java.util.function.Consumer;
import org.springframework.util.StringUtils;

final class ServiceDataHelper {

    private ServiceDataHelper() {
    }

    static void applyTextUpdate(String value, String field, Consumer<String> setter) {
        if (value == null) {
            return;
        }
        requireText(value, field);
        setter.accept(value.trim());
    }

    static void requireText(String value, String field) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
