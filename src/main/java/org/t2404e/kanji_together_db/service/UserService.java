package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.UserDTO;
import org.t2404e.kanji_together_db.entity.Clazz;
import org.t2404e.kanji_together_db.entity.Users;
import org.t2404e.kanji_together_db.repository.ClazzRepository;
import org.t2404e.kanji_together_db.repository.UsersRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ClazzRepository clazzRepository;

    // CREATE USER (Logic chính)
    public UserDTO createUser(UserDTO request) {
        // 1. Check trùng Email
        if (usersRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email đã tồn tại trong hệ thống");
        }

        Users user = new Users();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // 2. Set Default Values (Nếu null thì lấy mặc định)
        user.setHasEntranceExam(request.getHasEntranceExam() != null ? request.getHasEntranceExam() : false);
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        user.setIsVerified(request.getIsVerified() != null ? request.getIsVerified() : false);

        // 3. Gán lớp (nếu có)
        if (request.getClazzId() != null) {
            Clazz clazz = clazzRepository.findById(request.getClazzId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lớp học không tồn tại"));
            user.setClazz(clazz);
        }

        Users savedUser = usersRepository.save(user);
        return mapToDTO(savedUser);
    }

    // --- Các hàm khác giữ nguyên hoặc cập nhật mapToDTO ---

    public List<UserDTO> getAllUsers() {
        return usersRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user ID: " + id));
        return mapToDTO(user);
    }

    public UserDTO updateUser(Long id, UserDTO request) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user"));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getIsActive() != null) user.setIsActive(request.getIsActive());
        // Logic update lớp
        if (request.getClazzId() != null) {
            Clazz clazz = clazzRepository.findById(request.getClazzId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lớp không tồn tại"));
            user.setClazz(clazz);
        }

        return mapToDTO(usersRepository.save(user));
    }

    public void softDeleteUser(Long id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user"));
        user.setIsActive(false);
        usersRepository.save(user);
    }

    // Helper Convert
    private UserDTO mapToDTO(Users entity) {
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setHasEntranceExam(entity.getHasEntranceExam());
        dto.setIsActive(entity.getIsActive());
        dto.setIsVerified(entity.getIsVerified());
        dto.setCreateBy(entity.getCreateBy());
        dto.setEditBy(entity.getEditBy());

        if (entity.getClazz() != null) {
            dto.setClazzId(entity.getClazz().getId());
            dto.setClazzName(entity.getClazz().getName());
        }
        return dto;
    }
}