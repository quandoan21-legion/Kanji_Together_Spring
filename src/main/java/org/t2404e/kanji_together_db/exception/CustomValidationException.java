package org.t2404e.kanji_together_db.exception;

import java.util.Map;

public class CustomValidationException extends RuntimeException {
    private final Map<String, String> errors;

    public CustomValidationException(Map<String, String> errors) {
        super("Dữ liệu không hợp lệ");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}