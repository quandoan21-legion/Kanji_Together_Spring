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
        // Tìm xem chữ Kanji này đã có trong DB chưa
        Optional<KanjiCharacters> existingOpt = repository.findByKanji(dto.getKanji());

        KanjiCharacters entity;

        if (existingOpt.isPresent()) {
            // TRƯỜNG HỢP: Đã có trong DB
            KanjiCharacters existing = existingOpt.get();

            // Nếu đang HOẠT ĐỘNG -> Báo lỗi trùng lặp (Bad Request)
            if (Boolean.TRUE.equals(existing.getIsActive())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Chữ Kanji '" + dto.getKanji() + "' đã tồn tại và đang hoạt động!");
            }

            // Nếu đang BỊ XÓA (Inactive) -> Tận dụng lại bản ghi cũ để khôi phục
            entity = existing;
        } else {
            // TRƯỜNG HỢP: Chưa có trong DB -> Tạo mới hoàn toàn
            entity = new KanjiCharacters();
            entity.setKanji(dto.getKanji());
        }

        // Cập nhật thông tin mới (cho cả trường hợp tạo mới hoặc khôi phục)
        entity.setOnPronunciation(dto.getOnPronunciation());
        entity.setKunPronunciation(dto.getKunPronunciation());
        entity.setNumStrokes(dto.getNumStrokes());
        entity.setJlpt(dto.getJlpt());
        entity.setKanjiDescription(dto.getKanjiDescription());
        entity.setTranslation(dto.getTranslation());


        // QUAN TRỌNG: Luôn set Active = TRUE khi tạo mới hoặc khôi phục
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

            // Nếu tên mới trùng với một chữ đã có
            if (existingKanji.isPresent()) {
                // Nếu chữ kia đang Active -> Báo lỗi
                if (Boolean.TRUE.equals(existingKanji.get().getIsActive())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chữ Kanji '" + dto.getKanji() + "' đã được sử dụng!");
                }
                // (Nâng cao: Nếu chữ kia Inactive, có thể cho phép merge hoặc báo lỗi tùy nghiệp vụ. Ở đây mình báo lỗi cho an toàn)
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

        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());

        return mapToDTO(repository.save(entity));
    }

    // 5. DELETE
    public void delete(Long id) {
        KanjiCharacters entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Kanji để xóa"));

        entity.setIsActive(false);
        repository.save(entity);
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
        dto.setCreateAt(entity.getCreateAt());
        dto.setIsActive(entity.getIsActive());
        dto.setCreateBy(entity.getCreateBy());
        dto.setEditBy(entity.getEditBy());
        return dto;
    }
}