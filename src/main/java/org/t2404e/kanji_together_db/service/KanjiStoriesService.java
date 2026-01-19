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
    // 1. LẤY DANH SÁCH CÓ LỌC (Đã sửa: Thay Email bằng kanjiText)
    // =====================================================================
    public List<KanjiStoryDTO> getAllFiltered(String status, String kanjiText, Long kanjiId, int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("id").descending());

        // Xử lý chuỗi rỗng -> null để Repository bỏ qua điều kiện lọc đó
        String filterStatus = (status != null && !status.isEmpty()) ? status : null;
        String filterKanji = (kanjiText != null && !kanjiText.isEmpty()) ? kanjiText : null;

        // Gọi hàm findAllFiltered mới trong Repository
        Page<KanjiStories> storiesPage = storyRepo.findAllFiltered(filterStatus, filterKanji, kanjiId, pageable);

        return storiesPage.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // =====================================================================
    // 2. CÁC CHỨC NĂNG KHÁC (GIỮ NGUYÊN 100%)
    // =====================================================================

    // Lấy chi tiết bài viết
    public KanjiStoryDTO getById(Long id) {
        KanjiStories entity = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết với ID: " + id));
        return mapToDTO(entity);
    }

    // DUYỆT BÀI: Cập nhật nghĩa vào bảng KanjiCharacters
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

    // TỪ CHỐI
    public void reject(Long id, String reason) {
        KanjiStories story = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Story"));

        story.setStatus("rejected");
        // Có thể lưu reason vào DB nếu entity có trường đó, hiện tại chỉ in log
        System.out.println("ID " + id + " bị từ chối vì: " + reason);
        storyRepo.save(story);
    }

    // CREATE (Cho API Mobile/Web User)
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

    // UPDATE
    public KanjiStoryDTO update(Long id, KanjiStoryDTO dto) {
        KanjiStories story = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Story"));

        if (dto.getKanjiStory() != null) story.setKanjiStory(dto.getKanjiStory());
        if (dto.getStatus() != null) story.setStatus(dto.getStatus());

        return mapToDTO(storyRepo.save(story));
    }

    // DELETE
    public void delete(Long id) {
        KanjiStories story = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Story"));
        storyRepo.delete(story);
    }

    // Mapper Helper
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