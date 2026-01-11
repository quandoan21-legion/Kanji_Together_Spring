package org.t2404e.kanji_together_db.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus; // Import mới
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException; // Import mới (Dùng cái này thay cho cái file bạn thiếu)
import org.t2404e.kanji_together_db.dto.ClazzDTO;
import org.t2404e.kanji_together_db.entity.Clazz;
import org.t2404e.kanji_together_db.repository.ClazzRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClazzService {

    @Autowired
    private ClazzRepository clazzRepository;

    // 1. CREATE
    public ClazzDTO create(ClazzDTO dto) {
        Clazz clazz = new Clazz();
        clazz.setName(dto.getName());
        clazz.setDescription(dto.getDescription());
        clazz.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        Clazz savedClazz = clazzRepository.save(clazz);
        return mapToDTO(savedClazz);
    }

    // 2. GET DETAIL
    public ClazzDTO getDetail(Long id) {
        Clazz clazz = clazzRepository.findById(id)
                // SỬA Ở ĐÂY: Dùng ResponseStatusException có sẵn
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học có ID: " + id));
        return mapToDTO(clazz);
    }

    // 3. LIST
    public Page<ClazzDTO> getList(String keyword, Boolean isActive, Pageable pageable) {
        Specification<Clazz> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return clazzRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    // 4. UPDATE
    public ClazzDTO update(Long id, ClazzDTO dto) {
        Clazz clazz = clazzRepository.findById(id)
                // SỬA Ở ĐÂY
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp để sửa"));

        if (dto.getName() != null) clazz.setName(dto.getName());
        if (dto.getDescription() != null) clazz.setDescription(dto.getDescription());
        if (dto.getIsActive() != null) clazz.setIsActive(dto.getIsActive());

        return mapToDTO(clazzRepository.save(clazz));
    }

    // 5. DELETE
    public void delete(Long id) {
        Clazz clazz = clazzRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp để xóa"));
        clazz.setIsActive(false);
        clazzRepository.save(clazz);
    }

    private ClazzDTO mapToDTO(Clazz entity) {
        ClazzDTO dto = new ClazzDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreateAt());
        dto.setUpdatedAt(entity.getEditAt());
        return dto;
    }
}