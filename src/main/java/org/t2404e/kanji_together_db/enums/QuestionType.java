package org.t2404e.kanji_together_db.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum QuestionType {
    READING,    // Cách đọc Hiragana
    COMPOUND,   // Ghép từ vựng
    STORY,      // Câu chuyện gợi nhớ
    CONTEXT,    // Hoàn thành câu (Điền vào ngữ cảnh)
    ODD_ONE;     // Tìm từ khác loại (Odd One Out)

    @JsonCreator
    public static QuestionType from(String value) {
        if (value == null) {
            return null;
        }
        for (QuestionType type : QuestionType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid QuestionType: " + value);
    }
}
