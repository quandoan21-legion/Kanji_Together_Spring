package org.t2404e.kanji_together_db.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.KanjiStoryDTO;
import org.t2404e.kanji_together_db.entity.KanjiCharacters;
import org.t2404e.kanji_together_db.entity.KanjiStories;
import org.t2404e.kanji_together_db.repository.KanjiCharactersRepository;
import org.t2404e.kanji_together_db.repository.KanjiStoriesRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.t2404e.kanji_together_db.entity.Users;
import org.t2404e.kanji_together_db.repository.UsersRepository;

@Service
public class KanjiStoriesService {

    private final KanjiStoriesRepository storyRepo;
    private final KanjiCharactersRepository kanjiRepo;
    private final UsersRepository usersRepo;

    public KanjiStoriesService(KanjiStoriesRepository storyRepo, KanjiCharactersRepository kanjiRepo, UsersRepository usersRepo) {
        this.storyRepo = storyRepo;
        this.kanjiRepo = kanjiRepo;
        this.usersRepo = usersRepo;
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
    // 2. DUYỆT BÀI
    // =====================================================================
    @Transactional
    public KanjiStoryDTO approve(Long id, Map<String, Object> data) {
        KanjiStories story = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Story"));

        // Check if kanji already exists (from story creation)
        KanjiCharacters kanji = story.getKanjiCharacter();
        if (kanji == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Story chưa được liên kết với Kanji. Vui lòng kiểm tra lại kanji_id.");
        }

        // If admin provides kanji metadata, update it (optional)
        if (data != null && !data.isEmpty() && hasKanjiMetadata(data)) {
            updateKanjiMetadata(kanji, data);
        }

        // Simply approve the story
        story.setStatus("approved");
        story.setRejectReason(null);
        return mapToDTO(storyRepo.save(story));
    }

    private boolean hasKanjiMetadata(Map<String, Object> data) {
        return data.containsKey("translation") || data.containsKey("meaning") || 
               data.containsKey("onyomi") || data.containsKey("kunyomi");
    }

    @Transactional
    private void updateKanjiMetadata(KanjiCharacters kanji, Map<String, Object> data) {
        // Validate if any metadata is provided
        validateApproveData(data);

        // Update metadata if provided
        if (data.get("translation") != null) {
            kanji.setTranslation(data.get("translation").toString().trim().toUpperCase());
        }
        if (data.get("meaning") != null) {
            kanji.setMeaning(data.get("meaning").toString().trim());
        }
        if (data.get("onyomi") != null) {
            kanji.setOnPronunciation(data.get("onyomi").toString().trim());
        }
        if (data.get("kunyomi") != null) {
            kanji.setKunPronunciation(data.get("kunyomi").toString().trim());
        }
        if (data.get("radical") != null) {
            kanji.setRadical(data.get("radical").toString().trim());
        }
        if (data.get("writing_image_url") != null) {
            kanji.setWritingImageUrl(data.get("writing_image_url").toString().trim());
        }
        if (data.get("kanji_description") != null) {
            kanji.setKanjiDescription(data.get("kanji_description").toString().trim());
        }
        if (data.get("vocabulary") != null) {
            kanji.setVocabulary(data.get("vocabulary").toString().trim());
        }
        if (data.get("examples") != null) {
            kanji.setExamples(data.get("examples").toString().trim());
        }
        if (data.get("stroke_count") != null) {
            kanji.setNumStrokes(Integer.parseInt(data.get("stroke_count").toString()));
        }
        if (data.get("jlpt_level") != null) {
            kanji.setJlpt(Integer.parseInt(data.get("jlpt_level").toString()));
        }
        if (data.get("components") != null && !data.get("components").toString().trim().isEmpty()) {
            kanji.setComponents(data.get("components").toString().trim());
        }

        kanji.setIsActive(true);
        kanjiRepo.save(kanji);
    }

    private void validateApproveData(Map<String, Object> data) {
        Map<String, String> errors = new java.util.HashMap<>();

        // Only validate fields that are actually provided
        
        // --- KANJI (1 ký tự chữ Hán) - only if provided ---
        if (data.get("kanji") != null && !data.get("kanji").toString().trim().isEmpty()) {
            String kanji = data.get("kanji").toString().trim();
            if (!kanji.matches("^[\\u4E00-\\u9FAF]$")) {
                errors.put("kanji", "Kanji phải là duy nhất 1 ký tự chữ Hán (VD: 休)");
            }
        }

        // --- HÁN VIỆT (Chữ in hoa) - only if provided ---
        if (data.get("translation") != null && !data.get("translation").toString().trim().isEmpty()) {
            String translation = data.get("translation").toString().trim();
            if (!translation.matches("^[A-ZÀ-Ỹ\\s,]+$")) {
                errors.put("translation", "Chỉ chấp nhận CHỮ IN HOA và DẤU PHẨY (VD: 'ÚC, UẤT')");
            }
        }

        // --- SỐ NÉT (1 -> 60) - only if provided ---
        if (data.get("stroke_count") != null && !data.get("stroke_count").toString().trim().isEmpty()) {
            try {
                int strokes = Integer.parseInt(data.get("stroke_count").toString());
                if (strokes <= 0 || strokes > 60) errors.put("stroke_count", "Số nét phải từ 1 đến 60");
            } catch (Exception e) { errors.put("stroke_count", "Số nét không hợp lệ"); }
        }

        // --- JLPT (1 -> 5) - only if provided ---
        if (data.get("jlpt_level") != null && !data.get("jlpt_level").toString().trim().isEmpty()) {
            try {
                int jlpt = Integer.parseInt(data.get("jlpt_level").toString());
                if (jlpt < 1 || jlpt > 5) errors.put("jlpt_level", "JLPT phải từ 1 (N1) đến 5 (N5)");
            } catch (Exception e) { errors.put("jlpt_level", "JLPT không hợp lệ"); }
        }

        // --- BỘ THỦ (Ký tự + Tên in hoa) - only if provided ---
        if (data.get("radical") != null && !data.get("radical").toString().trim().isEmpty()) {
            String radical = data.get("radical").toString().trim();
            if (!radical.matches("^[\\u4E00-\\u9FAF]\\s+[A-ZÀ-Ỹ\\s,]+$")) {
                errors.put("radical", "Định dạng sai. VD: '鬯 SƯỞNG, SƯỚNG'");
            }
        }

        // --- ÂM ON (Katakana) & KUN (Hiragana) - only if provided ---
        if (data.get("onyomi") != null && !data.get("onyomi").toString().trim().isEmpty()) {
            String onyomi = data.get("onyomi").toString().trim();
            if (!onyomi.matches("^[\\u30A0-\\u30FF\\s.\\r\\n]+$")) {
                errors.put("onyomi", "Âm On chỉ được chứa Katakana, dấu chấm");
            }
        }
        if (data.get("kunyomi") != null && !data.get("kunyomi").toString().trim().isEmpty()) {
            String kunyomi = data.get("kunyomi").toString().trim();
            if (!kunyomi.matches("^[\\u3040-\\u309F\\s.\\r\\n]+$")) {
                errors.put("kunyomi", "Âm Kun chỉ được chứa Hiragana, dấu chấm");
            }
        }

        // --- TỪ VỰNG - only if provided ---
        if (data.get("vocabulary") != null && !data.get("vocabulary").toString().trim().isEmpty()) {
            validateListRegex(data.get("vocabulary").toString(),
                    "^[^-\\r\\n]*[\\u3000-\\u30FF\\u4E00-\\u9FAF]+[^-\\r\\n]*-[^-\\r\\n]+-[^-\\r\\n]+$",
                    "vocabulary", "Yêu cầu: [NHẬT]-[PHIÊN ÂM]-[NGHĨA]", errors);
        }

        // --- VÍ DỤ - only if provided ---
        if (data.get("examples") != null && !data.get("examples").toString().trim().isEmpty()) {
            validateListRegex(data.get("examples").toString(),
                    "^[^-\\r\\n]*[\\u3000-\\u30FF\\u4E00-\\u9FAF]+[^-\\r\\n]*-[^-\\r\\n]+$",
                    "examples", "Yêu cầu: [CÂU NHẬT]-[DỊCH VIỆT]", errors);
        }

        // --- URL ẢNH - only if provided ---
        if (data.get("writing_image_url") != null && !data.get("writing_image_url").toString().trim().isEmpty()) {
            String url = data.get("writing_image_url").toString().toLowerCase().trim();
            if (!url.startsWith("http") || !url.matches(".*\\.(gif|png|jpg|jpeg|svg)$")) {
                errors.put("writing_image_url", "URL phải bắt đầu bằng http và kết thúc bằng đuôi ảnh (.gif, .png...)");
            }
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
    @Transactional
    public void reject(Long id, String status, String reason) {
        KanjiStories story = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy"));

        String targetStatus = (status != null) ? status.trim().toLowerCase() : "rejected";

        // Bắt buộc phải có lý do nếu trạng thái là từ chối (rejected)
        if (targetStatus.equals("rejected")) {
            if (reason == null || reason.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lý do từ chối bài viết không được để trống.");
            }
            story.setRejectReason(reason.trim()); // Lưu lý do vào entity
        }

        if (targetStatus.equals("pending") || targetStatus.equals("rejected")) {
            story.setStatus(targetStatus);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ: " + targetStatus);
        }

        storyRepo.save(story);
    }

    public KanjiStoryDTO create(KanjiStoryDTO dto) {
        // Validate that kanji_id is provided (required field)
        if (dto.getKanjiId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kanji_id không được để trống");
        }
        
        // Validate that kanji_id exists
        KanjiCharacters kanji = kanjiRepo.findById(dto.getKanjiId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kanji ID " + dto.getKanjiId() + " không tồn tại"));
        
        // Validate that user_email is provided if we need to link to a user
        Users user = null;
        if (dto.getUserEmail() != null && !dto.getUserEmail().trim().isEmpty()) {
            user = usersRepo.findByEmail(dto.getUserEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng với email " + dto.getUserEmail() + " không tồn tại"));
        }
        
        KanjiStories story = new KanjiStories();
        story.setKanjiCharacter(kanji);
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
        story.setUser(user);
        story.setApprovalStatus(dto.getApprovalStatus() != null ? dto.getApprovalStatus() : "pending");
        
        // If approval_status is "approved", set status to "approved"
        if ("approved".equalsIgnoreCase(story.getApprovalStatus())) {
            story.setStatus("approved");
        }
        
        return mapToDTO(storyRepo.save(story));
    }

    public KanjiStoryDTO update(Long id, KanjiStoryDTO dto) {
        KanjiStories story = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết"));
        if (dto.getKanjiStory() != null) story.setKanjiStory(dto.getKanjiStory());
        if (dto.getStatus() != null) story.setStatus(dto.getStatus());
        if (dto.getKanjiText() != null) story.setKanjiText(dto.getKanjiText());
        if (dto.getApprovalStatus() != null) {
            story.setApprovalStatus(dto.getApprovalStatus());
            // If approval_status is "approved", set status to "approved"
            if ("approved".equalsIgnoreCase(dto.getApprovalStatus())) {
                story.setStatus("approved");
            }
        }
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
        dto.setRejectReason(entity.getRejectReason());
        dto.setApprovalStatus(entity.getApprovalStatus());
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