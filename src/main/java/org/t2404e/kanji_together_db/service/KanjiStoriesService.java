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

@Service
public class KanjiStoriesService {

    private final KanjiStoriesRepository storyRepo;
    private final KanjiCharactersRepository kanjiRepo;

    public KanjiStoriesService(KanjiStoriesRepository storyRepo, KanjiCharactersRepository kanjiRepo) {
        this.storyRepo = storyRepo;
        this.kanjiRepo = kanjiRepo;
    }

    public List<KanjiStoryDTO> getAll(int page, int size) {
        int pageIndex = Math.max(page, 0);
        int pageSize = size > 0 ? size : 20;
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by("id").descending());
        return storyRepo.findAll(pageable).getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // 1. Lấy danh sách có lọc (Dùng cho trang Index Ruby)
    public List<KanjiStoryDTO> getAllFiltered(String status, String email, Long kanjiId, int page, int size) {
        String normalizedStatus = StringUtils.hasText(status) ? status : null;
        String normalizedEmail = StringUtils.hasText(email) ? email : null;
        int pageIndex = Math.max(page, 0);
        int pageSize = size > 0 ? size : 20;
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by("id").descending());
        // Thêm tham số kanjiId vào hàm gọi repository
        Page<KanjiStories> storiesPage = storyRepo.findAllFiltered(normalizedStatus, normalizedEmail, kanjiId, pageable);
        return storiesPage.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // 2. Lấy chi tiết bài viết (Dùng cho nút "Vào duyệt" - Sửa lỗi 405)
    public KanjiStoryDTO getById(Long id) {
        KanjiStories entity = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết với ID: " + id));
        return mapToDTO(entity);
    }

    // 3. DUYỆT BÀI: Cập nhật nghĩa vào bảng KanjiCharacters
    @Transactional
    public KanjiStoryDTO approve(Long id, Map<String, Object> data) {
        KanjiStories story = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Story"));

        story.setStatus("approved");

        // CẬP NHẬT DỮ LIỆU SANG BẢNG KANJI TỔNG
        KanjiCharacters kanji = story.getKanjiCharacter();
        if (kanji != null) {
            if (data.get("meaning") != null) kanji.setMeaning(data.get("meaning").toString());
            if (data.get("translation") != null) kanji.setTranslation(data.get("translation").toString());
            if (data.get("onyomi") != null) kanji.setOnPronunciation(data.get("onyomi").toString());
            if (data.get("kunyomi") != null) kanji.setKunPronunciation(data.get("kunyomi").toString());
            if (data.get("stroke_count") != null) {
                kanji.setNumStrokes(Integer.parseInt(data.get("stroke_count").toString()));
            }
            if (data.get("jlpt_level") != null) {
                kanji.setJlpt(Integer.parseInt(data.get("jlpt_level").toString()));
            }
            kanjiRepo.save(kanji);
        }

        return mapToDTO(storyRepo.save(story));
    }

    // 4. TỪ CHỐI
    public void reject(Long id, String reason) {
        KanjiStories story = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Story"));

        story.setStatus("rejected");
        System.out.println("ID " + id + " bị từ chối vì: " + reason);
        storyRepo.save(story);
    }

    // 5. CREATE (Cho API Mobile/Web User)
    public KanjiStoryDTO create(KanjiStoryDTO dto) {
        KanjiCharacters kanji = kanjiRepo.findById(dto.getKanjiId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Kanji"));

        KanjiStories story = new KanjiStories();
        story.setKanjiStory(dto.getKanjiStory());
        story.setKanjiCharacter(kanji);
        story.setStatus("pending");
        story.setIsActive(true);

        return mapToDTO(storyRepo.save(story));
    }
    public KanjiStoryDTO update(Long id, KanjiStoryDTO dto) {
        KanjiStories story = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Story"));

        if (dto.getKanjiStory() != null) story.setKanjiStory(dto.getKanjiStory());
        if (dto.getStatus() != null) story.setStatus(dto.getStatus());

        return mapToDTO(storyRepo.save(story));
    }
    // 6. DELETE
    public void delete(Long id) {
        KanjiStories story = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Story"));
        storyRepo.delete(story);
    }

    // Mapper Helper Duy nhất: Chuyển đổi Entity sang DTO
    private KanjiStoryDTO mapToDTO(KanjiStories entity) {
        KanjiStoryDTO dto = new KanjiStoryDTO();
        dto.setId(entity.getId());
        dto.setKanjiStory(entity.getKanjiStory());
        dto.setStatus(entity.getStatus());
        dto.setIsActive(entity.getIsActive());
        dto.setCreateAt(entity.getCreateAt());

        if (entity.getUser() != null) {
            dto.setUserEmail(entity.getUser().getEmail());
        }

        if (entity.getKanjiCharacter() != null) {
            dto.setKanjiId(entity.getKanjiCharacter().getId());
            dto.setKanjiText(entity.getKanjiCharacter().getKanji());
        }
        return dto;
    }
}
