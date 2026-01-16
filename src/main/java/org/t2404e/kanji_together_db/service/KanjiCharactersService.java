package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.KanjiCharacterDTO;
import org.t2404e.kanji_together_db.entity.KanjiCharacters;
import org.t2404e.kanji_together_db.repository.KanjiCharactersRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KanjiCharactersService {

    @Autowired
    private KanjiCharactersRepository repository;

    // 1. READ ALL
    public List<KanjiCharacterDTO> getAll() {
        return repository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // 2. READ DETAIL
    public KanjiCharacterDTO getDetail(Long id) {
        KanjiCharacters entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Kanji ID: " + id));
        return mapToDTO(entity);
    }

    // 3. CREATE (SỬA LOGIC: Khôi phục nếu đã xóa mềm)
    public KanjiCharacterDTO create(KanjiCharacterDTO dto) {
        Optional<KanjiCharacters> existingOpt = repository.findByKanji(dto.getKanji());

        KanjiCharacters entity;

        if (existingOpt.isPresent()) {
            KanjiCharacters existing = existingOpt.get();

            if (Boolean.TRUE.equals(existing.getIsActive())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Chữ Kanji '" + dto.getKanji() + "' đã tồn tại và đang hoạt động!");
            }

            entity = existing;
        } else {
            entity = new KanjiCharacters();
            entity.setKanji(dto.getKanji());
        }

        // ============ BỔ SUNG CÁC TRƯỜNG CÒN THIẾU ============
        entity.setOnPronunciation(dto.getOnPronunciation());
        entity.setKunPronunciation(dto.getKunPronunciation());
        entity.setNumStrokes(dto.getNumStrokes());
        entity.setJlpt(dto.getJlpt());
        entity.setKanjiDescription(dto.getKanjiDescription());
        entity.setTranslation(dto.getTranslation());

        // ✅ THÊM CÁC TRƯỜNG NÀY (đây là nguyên nhân!)
        entity.setMeaning(dto.getMeaning());
        entity.setRadical(dto.getRadical());
        entity.setComponents(dto.getComponents());
        entity.setWritingImageUrl(dto.getWritingImageUrl());
        // ======================================================

        entity.setIsActive(true);

        return mapToDTO(repository.save(entity));
    }

    // 4. UPDATE
    public KanjiCharacterDTO update(Long id, KanjiCharacterDTO dto) {
        KanjiCharacters entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Kanji để sửa"));

        // Logic check trùng khi đổi tên Kanji
        if (dto.getKanji() != null && !dto.getKanji().equals(entity.getKanji())) {
            Optional<KanjiCharacters> existingKanji = repository.findByKanji(dto.getKanji());

            if (existingKanji.isPresent()) {
                if (Boolean.TRUE.equals(existingKanji.get().getIsActive())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chữ Kanji '" + dto.getKanji() + "' đã được sử dụng!");
                }
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chữ Kanji '" + dto.getKanji() + "' đã tồn tại (đang bị ẩn). Vui lòng khôi phục nó thay vì sửa chữ này.");
            }
            entity.setKanji(dto.getKanji());
        }

        if (dto.getOnPronunciation() != null) entity.setOnPronunciation(dto.getOnPronunciation());
        if (dto.getKunPronunciation() != null) entity.setKunPronunciation(dto.getKunPronunciation());
        if (dto.getNumStrokes() != null) entity.setNumStrokes(dto.getNumStrokes());
        if (dto.getJlpt() != null) entity.setJlpt(dto.getJlpt());
        if (dto.getKanjiDescription() != null) entity.setKanjiDescription(dto.getKanjiDescription());
        if (dto.getTranslation() != null) entity.setTranslation(dto.getTranslation());

        // ✅ BỔ SUNG CÁC TRƯỜNG CÒN THIẾU TRONG UPDATE
        if (dto.getMeaning() != null) entity.setMeaning(dto.getMeaning());
        if (dto.getRadical() != null) entity.setRadical(dto.getRadical());
        if (dto.getComponents() != null) entity.setComponents(dto.getComponents());
        if (dto.getWritingImageUrl() != null) entity.setWritingImageUrl(dto.getWritingImageUrl());
        // ======================================================

        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());

        return mapToDTO(repository.save(entity));
    }

    // 5. DELETE (XÓA MỀM)
    public void delete(Long id) {
        KanjiCharacters entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Kanji để xóa"));

        entity.setIsActive(false);
        repository.save(entity);
    }

    // ============ MAP TO DTO (BỔ SUNG CÁC TRƯỜNG) ============
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
        // ======================================================

        dto.setCreateAt(entity.getCreateAt());
        dto.setIsActive(entity.getIsActive());
        dto.setCreateBy(entity.getCreateBy());
        dto.setEditBy(entity.getEditBy());
        return dto;
    }
}
