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

    // ==================================================================================
    // 1. PHẦN TỰ ĐỘNG LÀM SẠCH DỮ LIỆU
    // ==================================================================================
    private void normalizeData(KanjiCharacterDTO dto) {
        if (dto.getKanji() != null) dto.setKanji(dto.getKanji().trim());

        if (dto.getTranslation() != null) dto.setTranslation(cleanText(dto.getTranslation()));
        if (dto.getMeaning() != null) dto.setMeaning(cleanText(dto.getMeaning()));
        if (dto.getRadical() != null) dto.setRadical(cleanText(dto.getRadical()));
        if (dto.getComponents() != null) dto.setComponents(cleanText(dto.getComponents()));
        if (dto.getVocabulary() != null) dto.setVocabulary(cleanText(dto.getVocabulary()));
        if (dto.getExamples() != null) dto.setExamples(cleanText(dto.getExamples()));
        if (dto.getKanjiDescription() != null) dto.setKanjiDescription(cleanText(dto.getKanjiDescription()));
        if (dto.getWritingImageUrl() != null) dto.setWritingImageUrl(dto.getWritingImageUrl().trim());
    }

    private String cleanText(String input) {
        if (input == null) return null;
        return input.trim()
                .replaceAll("[ \\t]+", " ")
                .replaceAll(",\\s*", ", ");
    }

    // ==================================================================================
    // 2. PHẦN VALIDATE DỮ LIỆU
    // ==================================================================================
    private void validateKanjiData(KanjiCharacterDTO dto) {
        Map<String, String> errors = new HashMap<>();

        // --- KANJI ---
        if (isEmpty(dto.getKanji())) {
            errors.put("kanji", "Vui lòng điền chữ Kanji");
        } else if (!dto.getKanji().matches("^[\\u4E00-\\u9FAF]$")) {
            errors.put("kanji", "Kanji phải là duy nhất 1 ký tự chữ Hán (VD: 休)");
        }

        // --- HÁN VIỆT  ---
        if (isEmpty(dto.getTranslation())) {
            errors.put("translation", "Vui lòng điền âm Hán Việt");
        } else {
            if (dto.getTranslation().length() > 100) errors.put("translation", "Hán Việt quá dài (tối đa 100 ký tự)");

            String[] lines = dto.getTranslation().split("\\r?\\n");
            for (String line : lines) {
                if (!line.trim().isEmpty() && !line.matches("^[A-ZÀ-Ỹ\\s,]+$")) {
                    errors.put("translation", "Dòng '" + line + "' chứa ký tự cấm. Chỉ chấp nhận: CHỮ IN HOA và DẤU PHẨY (VD: 'ÚC, UẤT').");
                    break;
                }
            }
        }

        // --- JLPT ---
        if (dto.getJlpt() == null) errors.put("jlpt", "Vui lòng chọn cấp độ JLPT");
        else if (dto.getJlpt() < 1 || dto.getJlpt() > 5) errors.put("jlpt", "Cấp độ JLPT phải từ N5 đến N1");

        // --- SỐ NÉT (1 -> 60) ---
        if (dto.getNumStrokes() == null) {
            errors.put("num_strokes", "Vui lòng điền số nét");
        } else if (dto.getNumStrokes() <= 0 || dto.getNumStrokes() > 60) {
            errors.put("num_strokes", "Số nét không hợp lý (từ 1 đến 60)");
        }

        // --- LINK ẢNH (Http & Đuôi ảnh) ---
        if (isEmpty(dto.getWritingImageUrl())) {
            errors.put("writing_image_url", "Vui lòng điền URL ảnh cách viết");
        } else {
            String url = dto.getWritingImageUrl().toLowerCase();
            if (!url.startsWith("http")) {
                errors.put("writing_image_url", "Link ảnh phải bắt đầu bằng http/https");
            } else if (!url.matches(".*\\.(gif|png|jpg|jpeg|svg)$")) {
                errors.put("writing_image_url", "Link phải kết thúc bằng đuôi ảnh (.gif, .png, .jpg, .svg)");
            }
        }

        // --- NGHĨA (Max 255) ---
        if (isEmpty(dto.getMeaning())) {
            errors.put("meaning", "Vui lòng điền nghĩa tiếng Việt");
        } else if (dto.getMeaning().length() > 255) {
            errors.put("meaning", "Nghĩa quá dài (tối đa 255 ký tự)");
        }

        // --- BỘ THỦ (Cho phép dấu phẩy) ---
        if (!isEmpty(dto.getRadical()) && !dto.getRadical().trim().matches("^[\\u4E00-\\u9FAF]\\s+[A-ZÀ-Ỹ\\s,]+$")) {
            errors.put("radical", "Định dạng sai. VD: '鬯 SƯỞNG, SƯỚNG' (Chữ Hán + Tên In Hoa + Dấu Phẩy)");
        }

        if (isEmpty(dto.getKanjiDescription())) errors.put("kanji_description", "Vui lòng điền câu chuyện ghi nhớ");

        // --- TỪ VỰNG ---
        if (isEmpty(dto.getVocabulary())) {
            errors.put("vocabulary", "Vui lòng nhập ít nhất 1 từ vựng");
        } else {
            validateListStructure(dto.getVocabulary(),
                    "^[^-\\r\\n]*[\\u3000-\\u30FF\\u4E00-\\u9FAF]+[^-\\r\\n]*-[^-\\r\\n]+-[^-\\r\\n]+$",
                    "vocabulary", "Sai cấu trúc. Yêu cầu: [TIẾNG NHẬT]-[PHIÊN ÂM]-[NGHĨA] (2 dấu gạch ngang)", errors);
        }

        // --- VÍ DỤ ---
        if (isEmpty(dto.getExamples())) {
            errors.put("examples", "Vui lòng nhập ít nhất 1 câu ví dụ");
        } else {
            validateListStructure(dto.getExamples(),
                    "^[^-\\r\\n]*[\\u3000-\\u30FF\\u4E00-\\u9FAF]+[^-\\r\\n]*-[^-\\r\\n]+$",
                    "examples", "Sai cấu trúc. Yêu cầu: [CÂU NHẬT]-[DỊCH VIỆT] (1 dấu gạch ngang)", errors);
        }

        // --- ÂM ON / KUN ---
        validatePronunciation(dto.getOnPronunciation(), "^[\\u30A0-\\u30FF\\s.\\r\\n]+$", "on_pronunciation", "Âm On chỉ được chứa Katakana, dấu chấm, xuống dòng", errors);
        validatePronunciation(dto.getKunPronunciation(), "^[\\u3040-\\u309F\\s.\\r\\n]+$", "kun_pronunciation", "Âm Kun chỉ được chứa Hiragana, dấu chấm, xuống dòng", errors);

        // --- BỘ THÀNH PHẦN  ---
        if (!isEmpty(dto.getComponents())) {
            if (dto.getComponents().length() > 500) errors.put("components", "Nội dung quá dài (tối đa 500 ký tự)");

            String[] lines = dto.getComponents().split("\\r?\\n");
            for (String line : lines) {
                if (!line.trim().isEmpty() && !line.matches("^[^\\p{P}\\p{S}\\d]\\s+\\p{Lu}\\p{Ll}*(?:\\s*,\\s*\\p{Lu}\\p{Ll}*)*$")) {
                    errors.put("components", "Dòng '" + line + "' sai định dạng. Yêu cầu: [Ký tự] [Viết Hoa Chữ Đầu]. VD: '木 Mộc, Cộc'.");
                    break;
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new CustomValidationException(errors);
        }
    }

    // ==================================================================================
    // 3. CÁC HÀM HỖ TRỢ (HELPER METHODS)
    // ==================================================================================

    // Hàm kiểm tra cấu trúc danh sách (Từ vựng, Ví dụ)
    private void validateListStructure(String content, String regex, String field, String msg, Map<String, String> errors) {
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            if (!line.trim().isEmpty() && !line.matches(regex)) {
                errors.put(field, "Dòng '" + line + "' " + msg);
                break;
            }
        }
    }

    // Hàm kiểm tra phát âm (On/Kun)
    private void validatePronunciation(String content, String regex, String field, String msg, Map<String, String> errors) {
        if (isEmpty(content)) {
            errors.put(field, "Vui lòng nhập dữ liệu");
        } else if (!content.matches(regex)) {
            errors.put(field, msg);
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