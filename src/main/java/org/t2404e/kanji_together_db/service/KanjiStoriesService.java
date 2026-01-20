package org.t2404e.kanji_together_db.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.KanjiStoryDTO;
import org.t2404e.kanji_together_db.entity.KanjiCharacters;
import org.t2404e.kanji_together_db.entity.KanjiStories;
import org.t2404e.kanji_together_db.repository.KanjiCharactersRepository;
import org.t2404e.kanji_together_db.repository.KanjiStoriesRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KanjiStoriesService {

    private final KanjiStoriesRepository storyRepo;
    private final KanjiCharactersRepository kanjiRepo;

    public KanjiStoriesService(KanjiStoriesRepository storyRepo, KanjiCharactersRepository kanjiRepo) {
        this.storyRepo = storyRepo;
        this.kanjiRepo = kanjiRepo;
    }

    // =====================================================================
    // 1. LẤY DANH SÁCH & CHI TIẾT
    // =====================================================================
    public List<KanjiStoryDTO> getAllFiltered(String status, String kanjiText, Long kanjiId, int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("id").descending());
        String filterStatus = (status != null && !status.isEmpty()) ? status : null;
        String filterKanji = (kanjiText != null && !kanjiText.isEmpty()) ? kanjiText : null;

        Page<KanjiStories> storiesPage = storyRepo.findAllFiltered(filterStatus, filterKanji, kanjiId, pageable);
        return storiesPage.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public KanjiStoryDTO getById(Long id) {
        KanjiStories entity = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết ID: " + id));
        return mapToDTO(entity);
    }

    // =====================================================================
    // 2. DUYỆT BÀI (SIẾT CHẶT VALIDATE GIỐNG FORM THÊM/SỬA)
    // =====================================================================
    @Transactional
    public KanjiStoryDTO approve(Long id, Map<String, Object> data) {
        KanjiStories story = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Story"));

        // Validate dữ liệu đầu vào nghiêm ngặt
        validateApproveData(data);

        KanjiCharacters kanji = story.getKanjiCharacter();
        if (kanji == null) {
            String text = (data.get("kanji") != null) ? data.get("kanji").toString() : story.getKanjiText();
            if (text == null || text.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mặt chữ Kanji không hợp lệ.");
            }
            kanji = kanjiRepo.findByKanji(text).orElse(new KanjiCharacters());
            kanji.setKanji(text);
        }

        // Cập nhật thông tin hệ thống (chuẩn hóa Hán Việt in hoa)
        kanji.setTranslation(data.get("translation").toString().trim().toUpperCase());
        kanji.setMeaning(data.get("meaning").toString().trim());
        kanji.setOnPronunciation(data.get("onyomi") != null ? data.get("onyomi").toString().trim() : "");
        kanji.setKunPronunciation(data.get("kunyomi") != null ? data.get("kunyomi").toString().trim() : "");
        kanji.setRadical(data.get("radical").toString().trim());
        kanji.setWritingImageUrl(data.get("writing_image_url").toString().trim());
        kanji.setKanjiDescription(data.get("kanji_description").toString().trim());
        kanji.setVocabulary(data.get("vocabulary").toString().trim());
        kanji.setExamples(data.get("examples").toString().trim());

        kanji.setNumStrokes(Integer.parseInt(data.get("stroke_count").toString()));
        kanji.setJlpt(Integer.parseInt(data.get("jlpt_level").toString()));

        if (data.get("components") != null) {
            kanji.setComponents(data.get("components").toString().trim());
        }

        kanji.setIsActive(true);
        kanjiRepo.save(kanji);

        story.setKanjiCharacter(kanji);
        story.setStatus("approved");
        return mapToDTO(storyRepo.save(story));
    }

    private void validateApproveData(Map<String, Object> data) {
        Map<String, String> errors = new java.util.HashMap<>();

        // 1. Kiểm tra trống các trường bắt buộc
        String[] requiredFields = {
                "kanji", "translation", "meaning", "onyomi", "kunyomi",
                "stroke_count", "jlpt_level", "radical", "writing_image_url",
                "kanji_description", "vocabulary", "examples"
        };

        for (String field : requiredFields) {
            if (data.get(field) == null || data.get(field).toString().trim().isEmpty()) {
                errors.put(field, "Trường này không được để trống");
            }
        }

        if (!errors.isEmpty()) throwCustomException(errors);

        // 2. VALIDATE CHI TIẾT THEO REGEX CỦA KANJI_CHARACTERS_SERVICE

        // --- KANJI (1 ký tự chữ Hán) ---
        String kanji = data.get("kanji").toString().trim();
        if (!kanji.matches("^[\\u4E00-\\u9FAF]$")) {
            errors.put("kanji", "Kanji phải là duy nhất 1 ký tự chữ Hán (VD: 休)");
        }

        // --- HÁN VIỆT (Chữ in hoa) ---
        String translation = data.get("translation").toString().trim();
        if (!translation.matches("^[A-ZÀ-Ỹ\\s,]+$")) {
            errors.put("translation", "Chỉ chấp nhận CHỮ IN HOA và DẤU PHẨY (VD: 'ÚC, UẤT')");
        }

        // --- SỐ NÉT (1 -> 60) ---
        try {
            int strokes = Integer.parseInt(data.get("stroke_count").toString());
            if (strokes <= 0 || strokes > 60) errors.put("stroke_count", "Số nét phải từ 1 đến 60");
        } catch (Exception e) { errors.put("stroke_count", "Số nét không hợp lệ"); }

        // --- JLPT (1 -> 5) ---
        try {
            int jlpt = Integer.parseInt(data.get("jlpt_level").toString());
            if (jlpt < 1 || jlpt > 5) errors.put("jlpt_level", "JLPT phải từ 1 (N1) đến 5 (N5)");
        } catch (Exception e) { errors.put("jlpt_level", "JLPT không hợp lệ"); }

        // --- BỘ THỦ (Ký tự + Tên in hoa) ---
        String radical = data.get("radical").toString().trim();
        if (!radical.matches("^[\\u4E00-\\u9FAF]\\s+[A-ZÀ-Ỹ\\s,]+$")) {
            errors.put("radical", "Định dạng sai. VD: '鬯 SƯỞNG, SƯỚNG'");
        }

        // --- ÂM ON (Katakana) & KUN (Hiragana) ---
        String onyomi = data.get("onyomi").toString().trim();
        if (!onyomi.matches("^[\\u30A0-\\u30FF\\s.\\r\\n]+$")) {
            errors.put("onyomi", "Âm On chỉ được chứa Katakana, dấu chấm");
        }
        String kunyomi = data.get("kunyomi").toString().trim();
        if (!kunyomi.matches("^[\\u3040-\\u309F\\s.\\r\\n]+$")) {
            errors.put("kunyomi", "Âm Kun chỉ được chứa Hiragana, dấu chấm");
        }

        // --- TỪ VỰNG ([NHẬT]-[PHẦN ÂM]-[NGHĨA]) ---
        validateListRegex(data.get("vocabulary").toString(),
                "^[^-\\r\\n]*[\\u3000-\\u30FF\\u4E00-\\u9FAF]+[^-\\r\\n]*-[^-\\r\\n]+-[^-\\r\\n]+$",
                "vocabulary", "Yêu cầu: [NHẬT]-[PHIÊN ÂM]-[NGHĨA]", errors);

        // --- VÍ DỤ ([CÂU NHẬT]-[DỊCH VIỆT]) ---
        validateListRegex(data.get("examples").toString(),
                "^[^-\\r\\n]*[\\u3000-\\u30FF\\u4E00-\\u9FAF]+[^-\\r\\n]*-[^-\\r\\n]+$",
                "examples", "Yêu cầu: [CÂU NHẬT]-[DỊCH VIỆT]", errors);

        // --- BỘ THÀNH PHẦN (Ký tự + Viết hoa chữ đầu) ---
        if (data.get("components") != null && !data.get("components").toString().trim().isEmpty()) {
            validateListRegex(data.get("components").toString(),
                    "^[^\\p{P}\\p{S}\\d]\\s+\\p{Lu}\\p{Ll}*(?:\\s*,\\s*\\p{Lu}\\p{Ll}*)*$",
                    "components", "Sai định dạng. VD: '木 Mộc, Cộc'", errors);
        }

        // --- URL ẢNH ---
        String url = data.get("writing_image_url").toString().toLowerCase().trim();
        if (!url.startsWith("http") || !url.matches(".*\\.(gif|png|jpg|jpeg|svg)$")) {
            errors.put("writing_image_url", "URL phải bắt đầu bằng http và kết thúc bằng đuôi ảnh (.gif, .png...)");
        }

        if (!errors.isEmpty()) throwCustomException(errors);
    }

    // Hàm hỗ trợ kiểm tra regex cho từng dòng
    private void validateListRegex(String content, String regex, String field, String msg, Map<String, String> errors) {
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            if (!line.trim().isEmpty() && !line.matches(regex)) {
                errors.put(field, "Dòng '" + line + "' sai định dạng. " + msg);
                break;
            }
        }
    }

    // Hàm ném lỗi
    private void throwCustomException(Map<String, String> errors) {
        String message = errors.entrySet().stream()
                .map(e -> e.getValue())
                .collect(java.util.stream.Collectors.joining("<br/>"));
        throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, message);
    }

    // =====================================================================
    // 3. CÁC CHỨC NĂNG CRUD CƠ BẢN
    // =====================================================================
    // KanjiStoriesService.java
    @Transactional
    public void reject(Long id, String status, String reason) { // Thêm String reason
        KanjiStories story = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy"));

        String targetStatus = (status != null) ? status.trim().toLowerCase() : "rejected";

        if (targetStatus.equals("pending") || targetStatus.equals("rejected")) {
            story.setStatus(targetStatus);
            // Nếu database có cột lý do, hãy lưu tại đây
            // story.setRejectReason(reason);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái sai");
        }
        storyRepo.save(story);
    }

    public KanjiStoryDTO create(KanjiStoryDTO dto) {
        KanjiStories story = new KanjiStories();
        story.setKanjiStory(dto.getKanjiStory());
        story.setKanjiText(dto.getKanjiText());
        story.setUserTranslation(dto.getUserTranslation());
        story.setUserMeaning(dto.getUserMeaning());
        story.setUserNumStrokes(dto.getUserNumStrokes());
        story.setUserOnyomi(dto.getUserOnyomi());
        story.setUserKunyomi(dto.getUserKunyomi());
        story.setUserRadical(dto.getUserRadical());
        story.setUserComponents(dto.getUserComponents());
        story.setUserVocabulary(dto.getUserVocabulary());
        story.setUserExamples(dto.getUserExamples());
        story.setStatus("pending");
        story.setIsActive(true);
        return mapToDTO(storyRepo.save(story));
    }

    public KanjiStoryDTO update(Long id, KanjiStoryDTO dto) {
        KanjiStories story = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết"));
        if (dto.getKanjiStory() != null) story.setKanjiStory(dto.getKanjiStory());
        if (dto.getStatus() != null) story.setStatus(dto.getStatus());
        if (dto.getKanjiText() != null) story.setKanjiText(dto.getKanjiText());
        return mapToDTO(storyRepo.save(story));
    }

    public void delete(Long id) {
        KanjiStories story = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết"));
        storyRepo.delete(story);
    }

    // =====================================================================
    // 4. MAPPER
    // =====================================================================
    private KanjiStoryDTO mapToDTO(KanjiStories entity) {
        KanjiStoryDTO dto = new KanjiStoryDTO();
        dto.setId(entity.getId());
        dto.setKanjiStory(entity.getKanjiStory());
        dto.setStatus(entity.getStatus());
        dto.setIsActive(entity.getIsActive());
        dto.setCreateAt(entity.getCreateAt());
        if (entity.getUser() != null) dto.setUserEmail(entity.getUser().getEmail());

        if (entity.getKanjiCharacter() != null) {
            dto.setKanjiId(entity.getKanjiCharacter().getId());
            dto.setKanjiText(entity.getKanjiCharacter().getKanji());
        } else {
            dto.setKanjiText(entity.getKanjiText());
        }

        dto.setUserTranslation(entity.getUserTranslation());
        dto.setUserMeaning(entity.getUserMeaning());
        dto.setUserNumStrokes(entity.getUserNumStrokes());
        dto.setUserOnyomi(entity.getUserOnyomi());
        dto.setUserKunyomi(entity.getUserKunyomi());
        dto.setUserRadical(entity.getUserRadical());
        dto.setUserComponents(entity.getUserComponents());
        dto.setUserVocabulary(entity.getUserVocabulary());
        dto.setUserExamples(entity.getUserExamples());
        return dto;
    }
}