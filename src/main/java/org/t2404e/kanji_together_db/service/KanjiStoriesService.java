package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.KanjiStoryDTO;
import org.t2404e.kanji_together_db.entity.KanjiCharacters;
import org.t2404e.kanji_together_db.entity.KanjiStories;
import org.t2404e.kanji_together_db.repository.KanjiCharactersRepository;
import org.t2404e.kanji_together_db.repository.KanjiStoriesRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KanjiStoriesService {

    @Autowired
    private KanjiStoriesRepository storyRepo;

    @Autowired
    private KanjiCharactersRepository kanjiRepo;

    // 1. READ ALL
    public List<KanjiStoryDTO> getAll() {
        return storyRepo.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // 2. CREATE
    public KanjiStoryDTO create(KanjiStoryDTO dto) {
        KanjiCharacters kanji = kanjiRepo.findById(dto.getKanjiId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Kanji ID: " + dto.getKanjiId()));

        KanjiStories story = new KanjiStories();
        story.setKanjiStory(dto.getKanjiStory());
        story.setKanjiCharacter(kanji);

        // MỚI: Mặc định là TRUE (Active) nếu Client không gửi
        story.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        return mapToDTO(storyRepo.save(story));
    }

    // 3. UPDATE
    public KanjiStoryDTO update(Long id, KanjiStoryDTO dto) {
        KanjiStories story = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Story để sửa"));

        if (dto.getKanjiStory() != null) {
            story.setKanjiStory(dto.getKanjiStory());
        }

        // Update link sang Kanji khác (nếu cần)
        if (dto.getKanjiId() != null) {
            KanjiCharacters kanji = kanjiRepo.findById(dto.getKanjiId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Kanji ID mới: " + dto.getKanjiId()));
            story.setKanjiCharacter(kanji);
        }

        // MỚI: Cho phép cập nhật trạng thái
        if (dto.getIsActive() != null) {
            story.setIsActive(dto.getIsActive());
        }

        return mapToDTO(storyRepo.save(story));
    }

    // 4. DELETE (Soft Delete)
    public void delete(Long id) {
        KanjiStories story = storyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Story để xóa"));

        // MỚI: Thay vì xóa vĩnh viễn, chỉ set active = false
        story.setIsActive(false);
        storyRepo.save(story);
    }

    // Mapper Helper
    private KanjiStoryDTO mapToDTO(KanjiStories entity) {
        KanjiStoryDTO dto = new KanjiStoryDTO();
        dto.setId(entity.getId());
        dto.setKanjiStory(entity.getKanjiStory());

        // MỚI: Map trạng thái trả về
        dto.setIsActive(entity.getIsActive());
        dto.setCreateAt(entity.getCreateAt());
        dto.setCreateBy(entity.getCreateBy());
        dto.setEditBy(entity.getEditBy());
        if (entity.getKanjiCharacter() != null) {
            dto.setKanjiId(entity.getKanjiCharacter().getId());
            dto.setKanjiText(entity.getKanjiCharacter().getKanji());
        }
        return dto;
    }
}