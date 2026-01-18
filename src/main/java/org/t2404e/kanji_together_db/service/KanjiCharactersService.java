package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.KanjiCharacterDTO;
import org.t2404e.kanji_together_db.entity.KanjiCharacters;
import org.t2404e.kanji_together_db.exception.CustomValidationException;
import org.t2404e.kanji_together_db.repository.KanjiCharactersRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KanjiCharactersService {

    @Autowired
    private KanjiCharactersRepository repository;

    public List<KanjiCharacterDTO> getAll() {
        return repository.findAllByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public KanjiCharacterDTO getDetail(Long id) {
        KanjiCharacters entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Kanji ID: " + id));
        return mapToDTO(entity);
    }

    public KanjiCharacterDTO create(KanjiCharacterDTO dto) {
        normalizeData(dto);
        validateKanjiData(dto);

        Optional<KanjiCharacters> existingOpt = repository.findByKanji(dto.getKanji());
        KanjiCharacters entity;

        if (existingOpt.isPresent()) {
            KanjiCharacters existing = existingOpt.get();
            if (Boolean.TRUE.equals(existing.getIsActive())) {
                Map<String, String> errors = new HashMap<>();
                errors.put("kanji", "Chữ Kanji '" + dto.getKanji() + "' đã tồn tại trong hệ thống!");
                throw new CustomValidationException(errors);
            }
            entity = existing;
        } else {
            entity = new KanjiCharacters();
            entity.setKanji(dto.getKanji());
        }

        updateEntityData(entity, dto);
        entity.setIsActive(true);
        return mapToDTO(repository.save(entity));
    }

    public KanjiCharacterDTO update(Long id, KanjiCharacterDTO dto) {
        normalizeData(dto);
        validateKanjiData(dto);

        KanjiCharacters entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Kanji để sửa"));

        if (dto.getKanji() != null && !dto.getKanji().equals(entity.getKanji())) {
            if (repository.findByKanji(dto.getKanji()).isPresent()) {
                Map<String, String> errors = new HashMap<>();
                errors.put("kanji", "Chữ Kanji mới này đã tồn tại!");
                throw new CustomValidationException(errors);
            }
            entity.setKanji(dto.getKanji());
        }

        updateEntityData(entity, dto);
        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());
        return mapToDTO(repository.save(entity));
    }

    public void delete(Long id) {
        KanjiCharacters entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy để xóa"));
        entity.setIsActive(false);
        repository.save(entity);
    }

    // --- HÀM TỰ ĐỘNG LÀM SẠCH ---
    private void normalizeData(KanjiCharacterDTO dto) {
        if (dto.getKanji() != null) dto.setKanji(dto.getKanji().trim());
        if (dto.getTranslation() != null) dto.setTranslation(cleanText(dto.getTranslation()));
        if (dto.getMeaning() != null) dto.setMeaning(cleanText(dto.getMeaning()));
        if (dto.getRadical() != null) dto.setRadical(cleanText(dto.getRadical()));
        if (dto.getComponents() != null) dto.setComponents(cleanText(dto.getComponents()));
        if (dto.getVocabulary() != null) dto.setVocabulary(cleanText(dto.getVocabulary()));
        if (dto.getExamples() != null) dto.setExamples(cleanText(dto.getExamples()));
        if (dto.getKanjiDescription() != null) dto.setKanjiDescription(cleanText(dto.getKanjiDescription()));
    }

    private String cleanText(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("[ \\t]+", " ");
    }

    private void validateKanjiData(KanjiCharacterDTO dto) {
        Map<String, String> errors = new HashMap<>();

        // 1. VALIDATE CƠ BẢN
        if (isEmpty(dto.getKanji())) {
            errors.put("kanji", "Vui lòng điền chữ Kanji");
        } else if (!dto.getKanji().matches("^[\\u4E00-\\u9FAF]$")) {
            errors.put("kanji", "Kanji phải là duy nhất 1 ký tự chữ Hán (VD: 休)");
        }

        if (isEmpty(dto.getTranslation())) {
            errors.put("translation", "Vui lòng điền âm Hán Việt");
        } else if (!dto.getTranslation().matches("^[A-ZÀ-Ỹ\\s]+$")) {
            errors.put("translation", "Hán Việt phải viết HOA TOÀN BỘ (VD: 'HƯU')");
        }

        if (dto.getJlpt() == null) errors.put("jlpt", "Vui lòng chọn cấp độ JLPT");
        else if (dto.getJlpt() < 1 || dto.getJlpt() > 5) errors.put("jlpt", "Cấp độ JLPT phải từ N5 đến N1");

        if (dto.getNumStrokes() == null) errors.put("num_strokes", "Vui lòng điền số nét");
        else if (dto.getNumStrokes() <= 0) errors.put("num_strokes", "Số nét phải lớn hơn 0");

        if (isEmpty(dto.getWritingImageUrl())) errors.put("writing_image_url", "Vui lòng điền URL ảnh cách viết");
        else if (!dto.getWritingImageUrl().startsWith("http")) errors.put("writing_image_url", "Link ảnh phải bắt đầu bằng http/https");

        if (isEmpty(dto.getMeaning())) errors.put("meaning", "Vui lòng điền nghĩa tiếng Việt");
        if (isEmpty(dto.getRadical())) errors.put("radical", "Vui lòng điền bộ thủ");
        if (isEmpty(dto.getKanjiDescription())) errors.put("kanji_description", "Vui lòng điền câu chuyện ghi nhớ");

        // --- 2. VALIDATE TỪ VỰNG---
        if (isEmpty(dto.getVocabulary())) {
            errors.put("vocabulary", "Vui lòng nhập ít nhất 1 từ vựng");
        } else {
            String[] lines = dto.getVocabulary().split("\\r?\\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                String vocabRegex = "^[^-\\r\\n]*[\\u3000-\\u30FF\\u4E00-\\u9FAF]+[^-\\r\\n]*-[^-\\r\\n]+-[^-\\r\\n]+$";

                if (!line.matches(vocabRegex)) {
                    errors.put("vocabulary", "Dòng '" + line + "' sai cấu trúc. Yêu cầu: [TIẾNG NHẬT]-[PHIÊN ÂM]-[NGHĨA] (Đúng 2 dấu gạch ngang).");
                    break;
                }
            }
        }

        // --- 3. VALIDATE CÂU VÍ DỤ  ---
        if (isEmpty(dto.getExamples())) {
            errors.put("examples", "Vui lòng nhập ít nhất 1 câu ví dụ");
        } else {
            String[] lines = dto.getExamples().split("\\r?\\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                String exampleRegex = "^[^-\\r\\n]*[\\u3000-\\u30FF\\u4E00-\\u9FAF]+[^-\\r\\n]*-[^-\\r\\n]+$";

                if (!line.matches(exampleRegex)) {
                    errors.put("examples", "Dòng '" + line + "' sai cấu trúc. Yêu cầu: [CÂU CÓ CHỮ NHẬT]-[DỊCH VIỆT] (Đúng 1 dấu gạch ngang).");
                    break;
                }
            }
        }

        // 4. VALIDATE ON/KUN
        if (isEmpty(dto.getOnPronunciation())) {
            errors.put("on_pronunciation", "Vui lòng điền âm On");
        } else {
            if (!dto.getOnPronunciation().matches("^[\\u30A0-\\u30FF\\s.\\r\\n]+$")) {
                errors.put("on_pronunciation", "Âm On chỉ được chứa Katakana, dấu chấm, xuống dòng");
            }
        }

        if (isEmpty(dto.getKunPronunciation())) {
            errors.put("kun_pronunciation", "Vui lòng điền âm Kun");
        } else {
            if (!dto.getKunPronunciation().matches("^[\\u3040-\\u309F\\s.\\r\\n]+$")) {
                errors.put("kun_pronunciation", "Âm Kun chỉ được chứa Hiragana, dấu chấm, xuống dòng");
            }
        }

        // 5. VALIDATE BỘ THỦ
        if (!isEmpty(dto.getRadical()) && !dto.getRadical().trim().matches("^[\\u4E00-\\u9FAF]\\s+[A-ZÀ-Ỹ\\s]+$")) {
            errors.put("radical", "Định dạng đúng: [Chữ Hán] [Tên In Hoa Toàn Bộ] (VD: 鬯 SƯỞNG).");
        }

        // 6. VALIDATE BỘ THÀNH PHẦN
        if (!isEmpty(dto.getComponents())) {
            String[] lines = dto.getComponents().split("\\r?\\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                if (!line.matches("^[^,\\p{S}\\d]\\s+\\p{Lu}[^,]*$")) {
                    errors.put("components", "Dòng '" + line + "' sai. Yêu cầu: [Ký tự] [Viết hoa chữ đầu]. Không dùng dấu phẩy (hệ thống tự thêm).");
                    break;
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new CustomValidationException(errors);
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private void updateEntityData(KanjiCharacters entity, KanjiCharacterDTO dto) {
        entity.setOnPronunciation(dto.getOnPronunciation());
        entity.setKunPronunciation(dto.getKunPronunciation());
        entity.setNumStrokes(dto.getNumStrokes());
        entity.setJlpt(dto.getJlpt());
        entity.setKanjiDescription(dto.getKanjiDescription());
        entity.setTranslation(dto.getTranslation());
        entity.setMeaning(dto.getMeaning());
        entity.setRadical(dto.getRadical());
        entity.setComponents(dto.getComponents());
        entity.setWritingImageUrl(dto.getWritingImageUrl());
        entity.setVocabulary(dto.getVocabulary());
        entity.setExamples(dto.getExamples());
    }

    private KanjiCharacterDTO mapToDTO(KanjiCharacters entity) {
        KanjiCharacterDTO dto = new KanjiCharacterDTO();
        dto.setId(entity.getId());
        dto.setKanji(entity.getKanji());
        dto.setOnPronunciation(entity.getOnPronunciation());
        dto.setKunPronunciation(entity.getKunPronunciation());
        dto.setNumStrokes(entity.getNumStrokes());
        dto.setJlpt(entity.getJlpt());
        dto.setKanjiDescription(entity.getKanjiDescription());
        dto.setTranslation(entity.getTranslation());
        dto.setMeaning(entity.getMeaning());
        dto.setRadical(entity.getRadical());
        dto.setComponents(entity.getComponents());
        dto.setWritingImageUrl(entity.getWritingImageUrl());
        dto.setVocabulary(entity.getVocabulary());
        dto.setExamples(entity.getExamples());
        dto.setCreateAt(entity.getCreateAt());
        dto.setIsActive(entity.getIsActive());
        dto.setCreateBy(entity.getCreateBy());
        dto.setEditBy(entity.getEditBy());
        return dto;
    }
}