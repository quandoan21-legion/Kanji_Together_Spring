package org.t2404e.kanji_together_db.exception;

import lombok.Getter;
import java.util.Map;

@Getter
public class CustomValidationException extends RuntimeException {
    private final Map<String, String> errors;

    public CustomValidationException(Map<String, String> errors) {
        this.errors = errors;
    }
}