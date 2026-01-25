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

    // Lấy danh sách (Giữ nguyên)
    public List<KanjiCharacterDTO> getAll(String keyword, Boolean isActive, String status) {
        List<KanjiCharacters> list = repository.searchAndFilter(keyword, isActive, status);
        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Lấy danh sách đóng góp của 1 chữ (Dành cho Admin xem lịch sử)
    public List<KanjiCharacterDTO> getContributions(String kanji) {
        return repository.findContributions(kanji).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ADMIN: Duyệt bài (Có Validate dữ liệu đầu vào)
    public KanjiCharacterDTO approve(Long pendingId, KanjiCharacterDTO finalDto) {
        // 1. Lấy bản ghi Pending (để cập nhật status lịch sử sau này)
        KanjiCharacters pendingEntity = repository.findById(pendingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài đóng góp ID: " + pendingId));

        // 2. CHUẨN HÓA & VALIDATE DỮ LIỆU ADMIN GỬI LÊN

        normalizeData(finalDto);
        validateKanjiData(finalDto); // Bắt buộc Validate chặt chẽ trước khi Active

        // 3. Xử lý Merge vào bản Gốc (Master)
        // Tìm xem chữ này đã có bản gốc (Active/Hidden) chưa dựa trên Kanji mà Admin gửi
        Optional<KanjiCharacters> masterOpt = repository.findByKanjiAndIsActiveTrue(finalDto.getKanji());

        KanjiCharacters masterEntity;

        if (masterOpt.isPresent()) {
            // --- CẬP NHẬT CHỮ ĐÃ CÓ ---
            masterEntity = masterOpt.get();
        } else {
            // --- TẠO BẢN GỐC MỚI TINH ---
            masterEntity = new KanjiCharacters();
            masterEntity.setKanji(finalDto.getKanji());
        }

        // 4. COPY DỮ LIỆU TỪ FORM (finalDto) VÀO BẢN GỐC
        // Lưu ý: Dùng updateEntityData với finalDto chứ KHÔNG dùng mapToDTO(pendingEntity)
        updateEntityData(masterEntity, finalDto);

        // Bắt buộc set Active và Status chuẩn
        masterEntity.setIsActive(true);
        masterEntity.setStatus("ACTIVE");

        // Lưu bản gốc
        repository.save(masterEntity);

        // 5. CẬP NHẬT LỊCH SỬ (Bản Pending cũ)
        // Nếu bản đóng góp khác bản gốc (khác ID), thì update status để lưu lịch sử
        if (!pendingEntity.getId().equals(masterEntity.getId())) {
            pendingEntity.setStatus("APPROVED");
            pendingEntity.setIsActive(false);
            repository.save(pendingEntity);
        }

        return mapToDTO(masterEntity);
    }

    // --- [LOGIC MỚI] TỪ CHỐI BÀI ĐÓNG GÓP ---
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

    // --- [LOGIC MỚI] TẠO BỞI ADMIN (QUẢN LÝ BẢN GỐC) ---
    public KanjiCharacterDTO create(KanjiCharacterDTO dto) {
        normalizeData(dto);
        validateKanjiData(dto);

        // Tìm tất cả các bản ghi của chữ này
        List<KanjiCharacters> existingList = repository.findAllByKanji(dto.getKanji());

        // Ưu tiên tìm bản đang ACTIVE hoặc HIDDEN (Là bản gốc)
        Optional<KanjiCharacters> masterOpt = existingList.stream()
                .filter(k -> "ACTIVE".equals(k.getStatus()) || "HIDDEN".equals(k.getStatus()))
                .findFirst();

        KanjiCharacters entity;

        if (masterOpt.isPresent()) {
            // Nếu đã có bản gốc -> Admin sửa đè lên bản gốc
            entity = masterOpt.get();
            if (Boolean.TRUE.equals(entity.getIsActive())) {
                // Nếu muốn chặn Admin tạo trùng chữ đang Active thì mở comment dưới,
                // còn logic hiện tại là cho phép sửa đè (Update).
                // throw new CustomValidationException(Map.of("kanji", "Chữ này đang hoạt động!"));
            }
        } else {
            // Chưa có bản gốc -> Tạo mới
            entity = new KanjiCharacters();
            entity.setKanji(dto.getKanji());
        }

        updateEntityData(entity, dto);
        entity.setIsActive(true);
        entity.setStatus("ACTIVE");

        return mapToDTO(repository.save(entity));
    }

    // --- [LOGIC MỚI] TẠO BỞI USER (LUÔN TẠO DÒNG PENDING MỚI) ---
    public KanjiCharacterDTO createForUser(KanjiCharacterDTO dto) {
        normalizeData(dto);
        validateKanjiOnly(dto);

        // KHÔNG check trùng, KHÔNG check status cũ.
        // Luôn tạo mới để đảm bảo tính công bằng cho người đóng góp.

        KanjiCharacters pendingEntity = new KanjiCharacters();
        pendingEntity.setKanji(dto.getKanji());

        // Cập nhật dữ liệu
        updateEntityDataIfPresent(pendingEntity, dto);

        // Luôn set trạng thái chờ duyệt
        pendingEntity.setIsActive(false);
        pendingEntity.setStatus("PENDING");

        return mapToDTO(repository.save(pendingEntity));
    }

    public KanjiCharacterDTO update(Long id, KanjiCharacterDTO dto) {
        normalizeData(dto);
        validateKanjiData(dto);

        KanjiCharacters entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Kanji để sửa"));

        // Nếu đổi chữ Kanji (Ví dụ sửa lỗi chính tả kanji)
        if (dto.getKanji() != null && !dto.getKanji().equals(entity.getKanji())) {
            // Chỉ báo lỗi nếu chữ mới đã có bản ACTIVE khác (tránh conflict 2 bản active)
            Optional<KanjiCharacters> activeConflict = repository.findByKanjiAndIsActiveTrue(dto.getKanji());
            if (activeConflict.isPresent()) {
                Map<String, String> errors = new HashMap<>();
                errors.put("kanji", "Chữ Kanji mới này đã có bản Active trong hệ thống!");
                throw new CustomValidationException(errors);
            }
            entity.setKanji(dto.getKanji());
        }

        updateEntityData(entity, dto);

        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());

        return mapToDTO(repository.save(entity));
    }

    // XÓA MỀM (Giữ nguyên)
    public void delete(Long id) {
        KanjiCharacters entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy"));
        entity.setStatus("DELETED");
        entity.setIsActive(false);
        repository.save(entity);
    }

    // ==================================================================================
    // 1. PHẦN TỰ ĐỘNG LÀM SẠCH DỮ LIỆU (Giữ nguyên)
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

        // 1. KANJI
        if (isEmpty(dto.getKanji())) {
            errors.put("kanji", "Chữ Kanji không được để trống");
        } else if (!dto.getKanji().matches("^[\\u4E00-\\u9FAF]$")) {
            errors.put("kanji", "Kanji phải là duy nhất 1 ký tự chữ Hán");
        }

        // 2. HÁN VIỆT
        if (isEmpty(dto.getTranslation())) {
            errors.put("translation", "Hán Việt không được để trống");
        } else if (!dto.getTranslation().matches("^[A-ZÀ-Ỹ\\s,]+$")) {
            errors.put("translation", "Hán Việt phải viết IN HOA (VD: HƯU, NGHI)");
        }

        // 3. JLPT
        if (dto.getJlpt() == null) {
            errors.put("jlpt", "Vui lòng chọn cấp độ JLPT");
        } else if (dto.getJlpt() < 1 || dto.getJlpt() > 5) {
            errors.put("jlpt", "Cấp độ JLPT phải từ N5 đến N1");
        }

        // 4. SỐ NÉT
        if (dto.getNumStrokes() == null) {
            errors.put("num_strokes", "Vui lòng nhập số nét");
        } else if (dto.getNumStrokes() <= 0 || dto.getNumStrokes() > 60) {
            errors.put("num_strokes", "Số nét phải từ 1 đến 60");
        }

        // 5. NGHĨA
        if (isEmpty(dto.getMeaning())) {
            errors.put("meaning", "Nghĩa tiếng Việt không được để trống");
        }

        // 6. ÂM ON
        if (isEmpty(dto.getOnPronunciation())) {
            errors.put("on_pronunciation", "Âm On không được để trống");
        } else if (!dto.getOnPronunciation().matches("^[\\u30A0-\\u30FF\\s.\\r\\n]+$")) {
            errors.put("on_pronunciation", "Âm On sai format (Phải là Katakana)");
        }

        // 7. ÂM KUN
        if (isEmpty(dto.getKunPronunciation())) {
            errors.put("kun_pronunciation", "Âm Kun không được để trống");
        } else if (!dto.getKunPronunciation().matches("^[\\u3040-\\u309F\\s.\\r\\n]+$")) {
            errors.put("kun_pronunciation", "Âm Kun sai format (Phải là Hiragana)");
        }

        // 8. URL ẢNH
        if (isEmpty(dto.getWritingImageUrl())) {
            errors.put("writing_image_url", "URL ảnh không được để trống");
        } else {
            String url = dto.getWritingImageUrl().toLowerCase();
            if (!url.startsWith("http") || !url.matches(".*\\.(gif|png|jpg|jpeg|svg)$")) {
                errors.put("writing_image_url", "URL không hợp lệ");
            }
        }

        // 9. BỘ THỦ
        if (isEmpty(dto.getRadical())) {
            errors.put("radical", "Bộ thủ không được để trống");
        } else if (!dto.getRadical().matches("^[\\u4E00-\\u9FAF]\\s+[A-ZÀ-Ỹ\\s,]+$")) {
            errors.put("radical", "Sai định dạng. VD đúng: '亻 NHÂN'");
        }

        // 10. CÂU CHUYỆN
        if (isEmpty(dto.getKanjiDescription())) {
            errors.put("kanji_description", "Câu chuyện không được để trống");
        }

        // 11. THÀNH PHẦN (Cái này là TÙY CHỌN -> Chỉ check format nếu có nhập)
        if (!isEmpty(dto.getComponents())) {
            String regex = "^[^\\p{P}\\p{S}\\d]\\s+\\p{Lu}\\p{Ll}*(?:\\s*,\\s*[^\\p{P}\\p{S}\\d]\\s+\\p{Lu}\\p{Ll}*)*$";
            if (!dto.getComponents().matches(regex)) {
                errors.put("components", "Sai định dạng. VD đúng: '木 Mộc, 目 Mục'");
            }
        }

        // =========================================================
        // SỬA Ở ĐÂY: CHUYỂN TỪ VỰNG VÀ VÍ DỤ THÀNH BẮT BUỘC
        // =========================================================

        // 12. TỪ VỰNG (BẮT BUỘC)
        if (isEmpty(dto.getVocabulary())) {
            errors.put("vocabulary", "Từ vựng không được để trống");
        } else {
            validateListRegex(dto.getVocabulary(),
                    "^[^-\\r\\n]*[\\u3000-\\u30FF\\u4E00-\\u9FAF]+[^-\\r\\n]*-[^-\\r\\n]+-[^-\\r\\n]+$",
                    "vocabulary", "Sai định dạng [NHẬT]-[PHIÊN ÂM]-[NGHĨA]", errors);
        }

        // 13. VÍ DỤ (BẮT BUỘC)
        if (isEmpty(dto.getExamples())) {
            errors.put("examples", "Ví dụ không được để trống");
        } else {
            validateListRegex(dto.getExamples(),
                    "^[^-\\r\\n]*[\\u3000-\\u30FF\\u4E00-\\u9FAF]+[^-\\r\\n]*-[^-\\r\\n]+$",
                    "examples", "Sai định dạng [CÂU NHẬT]-[DỊCH VIỆT]", errors);
        }

        // =========================================================

        // NÉM LỖI TỔNG HỢP
        if (!errors.isEmpty()) {
            throw new CustomValidationException(errors);
        }
    }

    // Đừng quên hàm hỗ trợ này
    private void validateListRegex(String content, String regex, String field, String msg, Map<String, String> errors) {
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            if (!line.trim().isEmpty() && !line.matches(regex)) {
                errors.put(field, "Dòng '" + line + "' bị sai. " + msg);
                break;
            }
        }
    }

    private void validatePronunciation(String content, String regex, String field, String msg, Map<String, String> errors) {
        if (isEmpty(content)) errors.put(field, "Vui lòng nhập dữ liệu");
        else if (!content.matches(regex)) errors.put(field, msg);
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
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
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
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
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