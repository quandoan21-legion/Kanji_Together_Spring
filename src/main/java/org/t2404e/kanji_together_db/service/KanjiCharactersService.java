package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    // Lấy danh sách (Giữ nguyên)
    public List<KanjiCharacterDTO> getAll(String keyword, Boolean isActive, String status) {
        List<KanjiCharacters> list = repository.searchAndFilter(keyword, isActive, status);
        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Lấy danh sách với phân trang kiểu page (page size = 20)
    public List<KanjiCharacterDTO> getAll(String keyword, Boolean isActive, String status, Integer page) {
        if (page == null) {
            return getAll(keyword, isActive, status);
        }

        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page must be >= 0");
        }

        int limit = 20;
        int offset = limit * page;
        List<KanjiCharacters> list = repository.searchAndFilter(keyword, isActive, status, limit, offset);
        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Lấy danh sách đóng góp của 1 chữ (Dành cho Admin xem lịch sử)
    public List<KanjiCharacterDTO> getContributions(String kanji) {
        return repository.findContributions(kanji).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ==================================================================================
    // 1. LOGIC DUYỆT BÀI (APPROVE)
    // ==================================================================================
    @Transactional
    public KanjiCharacterDTO approve(Long pendingId, KanjiCharacterDTO finalDto) {
        // Lấy bản ghi đóng góp (Pending)
        KanjiCharacters pendingEntity = repository.findById(pendingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài đóng góp ID: " + pendingId));

        normalizeData(finalDto);
        validateKanjiData(finalDto);

        // TÌM BẢN GỐC (MASTER) - Tìm xuyên thấu mọi trạng thái (Sử dụng findFirstByKanji)
        Optional<KanjiCharacters> masterOpt = repository.findFirstByKanji(finalDto.getKanji());

        KanjiCharacters masterEntity;

        if (masterOpt.isPresent()) {
            masterEntity = masterOpt.get();

            // QUY TẮC: Nếu đã có và đang ACTIVE -> Chỉ cập nhật các trường bổ sung
            if ("ACTIVE".equals(masterEntity.getStatus()) && Boolean.TRUE.equals(masterEntity.getIsActive())) {
                updateSupplementaryFields(masterEntity, finalDto);
            } else {
                // QUY TẮC: Nếu đang ẨN hoặc ĐÃ XÓA -> Được phép sửa hết tất cả các trường
                updateFullEntityData(masterEntity, finalDto);
            }
        } else {
            // QUY TẮC: Nếu chưa có trên DB -> Tạo mới hoàn toàn
            masterEntity = new KanjiCharacters();
            masterEntity.setKanji(finalDto.getKanji());
            updateFullEntityData(masterEntity, finalDto);
        }

        // Bắt buộc set Active và Status chuẩn khi Duyệt
        masterEntity.setIsActive(true);
        masterEntity.setStatus("ACTIVE");
        repository.save(masterEntity);

        // CẬP NHẬT LỊCH SỬ: Đánh dấu bản đóng góp cũ là APPROVED
        if (!pendingEntity.getId().equals(masterEntity.getId())) {
            pendingEntity.setStatus("APPROVED");
            pendingEntity.setIsActive(false);
            repository.save(pendingEntity);
        }

        return mapToDTO(masterEntity);
    }

    // --- TỪ CHỐI BÀI ĐÓNG GÓP ---
    public void reject(Long id) {
        KanjiCharacters entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy"));
        entity.setStatus("REJECTED");
        entity.setIsActive(false);
        repository.save(entity);
    }

    public KanjiCharacterDTO getDetail(Long id) {
        KanjiCharacters entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Kanji ID: " + id));
        return mapToDTO(entity);
    }

    // ==================================================================================
    // 2. LOGIC TẠO/SỬA BỞI ADMIN
    // ==================================================================================
    public KanjiCharacterDTO create(KanjiCharacterDTO dto) {
        // 1. Chuẩn hóa và Validate dữ liệu đầu vào
        normalizeData(dto);
        validateKanjiData(dto);

        // 2. Tìm bản gốc bất kể trạng thái
        Optional<KanjiCharacters> masterOpt = repository.findFirstByKanji(dto.getKanji());
        KanjiCharacters entity;

        if (masterOpt.isPresent()) {
            entity = masterOpt.get();

            String currentStatus = entity.getStatus();


            // LOGIC MỚI: Chặn cả ACTIVE và HIDDEN. Chỉ cho phép đi tiếp nếu là DELETED.
            if ("ACTIVE".equals(currentStatus) || "HIDDEN".equals(currentStatus)) {
                Map<String, String> errors = new HashMap<>();
                errors.put("kanji", "Chữ Kanji '" + dto.getKanji() + "' đã tồn tại (Trạng thái: " + currentStatus + "). Vui lòng tìm và sửa, không tạo mới!");
                throw new CustomValidationException(errors);
            }

            // Nếu xuống được đây nghĩa là status = DELETED -> Cho phép ghi đè để hồi sinh
            updateFullEntityData(entity, dto);
        } else {
            // Chưa có trong DB -> Tạo mới hoàn toàn

            entity = new KanjiCharacters();
            entity.setKanji(dto.getKanji());
            updateFullEntityData(entity, dto);
        }

        // Sau khi Create hoặc Hồi sinh, luôn đặt về trạng thái ACTIVE
        entity.setIsActive(true);
        entity.setStatus("ACTIVE");

        return mapToDTO(repository.save(entity));
    }

    public KanjiCharacterDTO update(Long id, KanjiCharacterDTO dto) {
        normalizeData(dto);
        validateKanjiData(dto);

        KanjiCharacters entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Kanji để sửa"));

        // Kiểm tra logic phân quyền sửa dựa trên trạng thái hiện tại của ID này
        if ("ACTIVE".equals(entity.getStatus()) && Boolean.TRUE.equals(entity.getIsActive())) {
            updateSupplementaryFields(entity, dto);
        } else {
            updateFullEntityData(entity, dto);
        }

        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());

        return mapToDTO(repository.save(entity));
    }

    // USER: Đóng góp (Luôn tạo bản ghi PENDING mới)
    public KanjiCharacterDTO createForUser(KanjiCharacterDTO dto) {
        normalizeData(dto);
        validateKanjiOnly(dto);

        KanjiCharacters pendingEntity = new KanjiCharacters();
        pendingEntity.setKanji(dto.getKanji());
        updateEntityDataIfPresent(pendingEntity, dto);

        pendingEntity.setIsActive(false);
        pendingEntity.setStatus("PENDING");

        return mapToDTO(repository.save(pendingEntity));
    }

    // XÓA MỀM
    public void delete(Long id) {
        KanjiCharacters entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy"));
        entity.setStatus("DELETED");
        entity.setIsActive(false);
        repository.save(entity);
    }

    // ==================================================================================
    // 3. CÁC HÀM CẬP NHẬT DỮ LIỆU PHÂN TẦNG
    // ==================================================================================

    // Cập nhật TOÀN BỘ (Dành cho bản mới hoặc bản Ẩn/Xóa)
    private void updateFullEntityData(KanjiCharacters entity, KanjiCharacterDTO dto) {
        entity.setTranslation(dto.getTranslation());
        entity.setMeaning(dto.getMeaning());
        entity.setJlpt(dto.getJlpt());
        entity.setNumStrokes(dto.getNumStrokes());
        entity.setRadical(dto.getRadical());
        entity.setComponents(dto.getComponents());
        entity.setOnPronunciation(dto.getOnPronunciation());
        entity.setKunPronunciation(dto.getKunPronunciation());

        // Gọi tiếp hàm cập nhật thông tin bổ sung
        updateSupplementaryFields(entity, dto);
    }

    // CHỈ cập nhật thông tin bổ sung (Dành cho bản đang ACTIVE)
    private void updateSupplementaryFields(KanjiCharacters entity, KanjiCharacterDTO dto) {
        entity.setKanjiDescription(dto.getKanjiDescription());
        entity.setVocabulary(dto.getVocabulary());
        entity.setExamples(dto.getExamples());
        entity.setWritingImageUrl(dto.getWritingImageUrl());
    }

    // ==================================================================================
    // 4. VALIDATION & NORMALIZATION (Giữ nguyên các chức năng cũ)
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
        return input.trim().replaceAll("[ \\t]+", " ").replaceAll(",\\s*", ", ");
    }

    private void validateKanjiData(KanjiCharacterDTO dto) {
        Map<String, String> errors = new HashMap<>();

        if (isEmpty(dto.getKanji())) {
            errors.put("kanji", "Chữ Kanji không được để trống");
        } else if (!dto.getKanji().matches("^[\\u4E00-\\u9FAF]$")) {
            errors.put("kanji", "Kanji phải là duy nhất 1 ký tự chữ Hán");
        }

        if (isEmpty(dto.getTranslation())) {
            errors.put("translation", "Hán Việt không được để trống");
        } else if (!dto.getTranslation().matches("^[A-ZÀ-Ỹ\\s,]+$")) {
            errors.put("translation", "Hán Việt phải viết IN HOA (VD: HƯU, NGHI)");
        }

        if (dto.getJlpt() == null) {
            errors.put("jlpt", "Vui lòng chọn cấp độ JLPT");
        } else if (dto.getJlpt() < 1 || dto.getJlpt() > 5) {
            errors.put("jlpt", "Cấp độ JLPT phải từ N5 đến N1");
        }

        if (dto.getNumStrokes() == null) {
            errors.put("num_strokes", "Vui lòng nhập số nét");
        } else if (dto.getNumStrokes() <= 0 || dto.getNumStrokes() > 60) {
            errors.put("num_strokes", "Số nét phải từ 1 đến 60");
        }

        if (isEmpty(dto.getMeaning())) {
            errors.put("meaning", "Nghĩa tiếng Việt không được để trống");
        }

        if (isEmpty(dto.getOnPronunciation())) {
            errors.put("on_pronunciation", "Âm On không được để trống");
        } else if (!dto.getOnPronunciation().matches("^[\\u30A0-\\u30FF\\s.\\r\\n]+$")) {
            errors.put("on_pronunciation", "Âm On sai format (Phải là Katakana)");
        }

        if (isEmpty(dto.getKunPronunciation())) {
            errors.put("kun_pronunciation", "Âm Kun không được để trống");
        } else if (!dto.getKunPronunciation().matches("^[\\u3040-\\u309F\\s.\\r\\n]+$")) {
            errors.put("kun_pronunciation", "Âm Kun sai format (Phải là Hiragana)");
        }

        if (isEmpty(dto.getWritingImageUrl())) {
            errors.put("writing_image_url", "URL ảnh không được để trống");
        } else {
            String url = dto.getWritingImageUrl().toLowerCase();
            if (!url.startsWith("http") || !url.matches(".*\\.(gif|png|jpg|jpeg|svg)$")) {
                errors.put("writing_image_url", "URL không hợp lệ");
            }
        }

        if (isEmpty(dto.getRadical())) {
            errors.put("radical", "Bộ thủ không được để trống");
        } else if (!dto.getRadical().matches("^[\\u4E00-\\u9FAF]\\s+[A-ZÀ-Ỹ\\s,]+$")) {
            errors.put("radical", "Sai định dạng. VD đúng: '亻 NHÂN'");
        }

        if (isEmpty(dto.getKanjiDescription())) {
            errors.put("kanji_description", "Câu chuyện không được để trống");
        }

        if (!isEmpty(dto.getComponents())) {
            String regex = "^[^\\p{P}\\p{S}\\d]\\s+\\p{Lu}\\p{Ll}*(?:\\s*,\\s*[^\\p{P}\\p{S}\\d]\\s+\\p{Lu}\\p{Ll}*)*$";
            if (!dto.getComponents().matches(regex)) {
                errors.put("components", "Sai định dạng. VD đúng: '木 Mộc, 目 Mục'");
            }
        }

        if (isEmpty(dto.getVocabulary())) {
            errors.put("vocabulary", "Từ vựng không được để trống");
        } else {
            validateListRegex(dto.getVocabulary(),
                    "^[^-\\r\\n]*[\\u3000-\\u30FF\\u4E00-\\u9FAF]+[^-\\r\\n]*-[^-\\r\\n]+-[^-\\r\\n]+$",
                    "vocabulary", "Sai định dạng [NHẬT]-[PHIÊN ÂM]-[NGHĨA]", errors);
        }

        if (isEmpty(dto.getExamples())) {
            errors.put("examples", "Ví dụ không được để trống");
        } else {
            validateListRegex(dto.getExamples(),
                    "^[^-\\r\\n]*[\\u3000-\\u30FF\\u4E00-\\u9FAF]+[^-\\r\\n]*-[^-\\r\\n]+$",
                    "examples", "Sai định dạng [CÂU NHẬT]-[DỊCH VIỆT]", errors);
        }

        if (!errors.isEmpty()) {
            throw new CustomValidationException(errors);
        }
    }

    private void validateListRegex(String content, String regex, String field, String msg, Map<String, String> errors) {
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            if (!line.trim().isEmpty() && !line.matches(regex)) {
                errors.put(field, "Dòng '" + line + "' bị sai. " + msg);
                break;
            }
        }
    }

    private void validateKanjiOnly(KanjiCharacterDTO dto) {
        Map<String, String> errors = new HashMap<>();
        if (isEmpty(dto.getKanji())) errors.put("kanji", "Vui lòng điền chữ Kanji");
        else if (!dto.getKanji().matches("^[\\u4E00-\\u9FAF]$")) errors.put("kanji", "Kanji phải là duy nhất 1 ký tự chữ Hán");
        if (!errors.isEmpty()) throw new CustomValidationException(errors);
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private void updateEntityDataIfPresent(KanjiCharacters entity, KanjiCharacterDTO dto) {
        if (dto.getOnPronunciation() != null) entity.setOnPronunciation(dto.getOnPronunciation());
        if (dto.getKunPronunciation() != null) entity.setKunPronunciation(dto.getKunPronunciation());
        if (dto.getNumStrokes() != null) entity.setNumStrokes(dto.getNumStrokes());
        if (dto.getJlpt() != null) entity.setJlpt(dto.getJlpt());
        if (dto.getKanjiDescription() != null) entity.setKanjiDescription(dto.getKanjiDescription());
        if (dto.getTranslation() != null) entity.setTranslation(dto.getTranslation());
        if (dto.getMeaning() != null) entity.setMeaning(dto.getMeaning());
        if (dto.getRadical() != null) entity.setRadical(dto.getRadical());
        if (dto.getComponents() != null) entity.setComponents(dto.getComponents());
        if (dto.getWritingImageUrl() != null) entity.setWritingImageUrl(dto.getWritingImageUrl());
        if (dto.getVocabulary() != null) entity.setVocabulary(dto.getVocabulary());
        if (dto.getExamples() != null) entity.setExamples(dto.getExamples());
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
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
