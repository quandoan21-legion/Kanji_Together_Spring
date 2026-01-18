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

    private void validateKanjiData(KanjiCharacterDTO dto) {
        Map<String, String> errors = new HashMap<>();

        // 1. KIỂM TRA BẮT BUỘC NHẬP
        if (isEmpty(dto.getKanji())) errors.put("kanji", "Vui lòng điền chữ Kanji");
        if (isEmpty(dto.getTranslation())) errors.put("translation", "Vui lòng điền âm Hán Việt");
        if (dto.getJlpt() == null) errors.put("jlpt", "Vui lòng chọn cấp độ JLPT");
        if (isEmpty(dto.getMeaning())) errors.put("meaning", "Vui lòng điền nghĩa tiếng Việt");
        if (dto.getNumStrokes() == null) errors.put("num_strokes", "Vui lòng điền số nét");
        if (isEmpty(dto.getRadical())) errors.put("radical", "Vui lòng điền bộ thủ");
        if (isEmpty(dto.getWritingImageUrl())) errors.put("writing_image_url", "Vui lòng điền URL ảnh cách viết");
        if (isEmpty(dto.getKanjiDescription())) errors.put("kanji_description", "Vui lòng điền câu chuyện ghi nhớ");

        // --- 2. VALIDATE TỪ VỰNG (MỚI: Phải có 2 dấu gạch ngang) ---
        if (isEmpty(dto.getVocabulary())) {
            errors.put("vocabulary", "Vui lòng nhập ít nhất 1 từ vựng");
        } else {
            String[] lines = dto.getVocabulary().split("\\r?\\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                // Regex: TỪ - PHIÊN ÂM - NGHĨA
                if (!line.matches("^.+-.+-.+$")) {
                    errors.put("vocabulary", "Sai định dạng dòng: '" + line + "'. Yêu cầu: [TỪ]-[PHIÊN ÂM]-[NGHĨA]");
                    break;
                }
            }
        }

        // --- 3. VALIDATE CÂU VÍ DỤ (MỚI: Phải có 1 dấu gạch ngang) ---
        if (isEmpty(dto.getExamples())) {
            errors.put("examples", "Vui lòng nhập ít nhất 1 câu ví dụ");
        } else {
            String[] lines = dto.getExamples().split("\\r?\\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                // Regex: CÂU - DỊCH
                if (!line.matches("^.+-.+$")) {
                    errors.put("examples", "Sai định dạng dòng: '" + line + "'. Yêu cầu: [CÂU NHẬT]-[DỊCH VIỆT]");
                    break;
                }
            }
        }

        // --- 4. VALIDATE ON/KUN (Cấm dấu phẩy) ---
        if (isEmpty(dto.getOnPronunciation())) {
            errors.put("on_pronunciation", "Vui lòng điền âm On");
        } else {
            String onRegex = "^[\\u30A0-\\u30FF\\s.\\r\\n]+$";
            if (!dto.getOnPronunciation().matches(onRegex)) {
                errors.put("on_pronunciation", "Âm On chỉ được chứa Katakana, dấu chấm và xuống dòng (Không dùng dấu phẩy)");
            }
        }

        if (isEmpty(dto.getKunPronunciation())) {
            errors.put("kun_pronunciation", "Vui lòng điền âm Kun");
        } else {
            String kunRegex = "^[\\u3040-\\u309F\\s.\\r\\n]+$";
            if (!dto.getKunPronunciation().matches(kunRegex)) {
                errors.put("kun_pronunciation", "Âm Kun chỉ được chứa Hiragana, dấu chấm và xuống dòng (Không dùng dấu phẩy)");
            }
        }

        // --- 5. KIỂM TRA ĐỊNH DẠNG BỘ THỦ ---
        String formatRegex = "^[\\u4E00-\\u9FAF]\\s+[A-ZÀ-Ỹ\\s]+$";
        if (!isEmpty(dto.getRadical()) && !dto.getRadical().trim().matches(formatRegex)) {
            errors.put("radical", "Định dạng đúng: [Chữ Hán] [Tên Hán Việt viết hoa] (VD: 鬯 SƯỞNG)");
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