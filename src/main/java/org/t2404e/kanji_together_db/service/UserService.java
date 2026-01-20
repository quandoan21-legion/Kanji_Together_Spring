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
        user.setUsername(request.getUsername());
        user.setDisplayName(request.getDisplayName());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());

        // 2. Set Default Values (Nếu null thì lấy mặc định)
        user.setHasEntranceExam(request.getHasEntranceExam() != null ? request.getHasEntranceExam() : false);
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        user.setIsVerified(request.getIsVerified() != null ? request.getIsVerified() : false);
        user.setLastLoginAt(request.getLastLoginAt());
        user.setPasswordHash(request.getPasswordHash());
        user.setAuthProvider(request.getAuthProvider());
        user.setStartDate(request.getStartDate());
        user.setAddressLine1(request.getAddressLine1());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setPostalCode(request.getPostalCode());
        user.setCountry(request.getCountry());

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

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (usersRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email đã tồn tại trong hệ thống");
            }
        }
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (usersRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username đã tồn tại trong hệ thống");
            }
        }

        if (request.getName() != null) user.setName(request.getName());
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getDisplayName() != null) user.setDisplayName(request.getDisplayName());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getHasEntranceExam() != null) user.setHasEntranceExam(request.getHasEntranceExam());
        if (request.getIsVerified() != null) user.setIsVerified(request.getIsVerified());
        if (request.getIsActive() != null) user.setIsActive(request.getIsActive());
        if (request.getLastLoginAt() != null) user.setLastLoginAt(request.getLastLoginAt());
        if (request.getPasswordHash() != null) user.setPasswordHash(request.getPasswordHash());
        if (request.getAuthProvider() != null) user.setAuthProvider(request.getAuthProvider());
        if (request.getStartDate() != null) user.setStartDate(request.getStartDate());
        if (request.getAddressLine1() != null) user.setAddressLine1(request.getAddressLine1());
        if (request.getCity() != null) user.setCity(request.getCity());
        if (request.getState() != null) user.setState(request.getState());
        if (request.getPostalCode() != null) user.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null) user.setCountry(request.getCountry());
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
        dto.setUsername(entity.getUsername());
        dto.setDisplayName(entity.getDisplayName());
        dto.setAvatarUrl(entity.getAvatarUrl());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setEmail(entity.getEmail());
        dto.setHasEntranceExam(entity.getHasEntranceExam());
        dto.setIsActive(entity.getIsActive());
        dto.setIsVerified(entity.getIsVerified());
        dto.setLastLoginAt(entity.getLastLoginAt());
        dto.setAuthProvider(entity.getAuthProvider());
        dto.setStartDate(entity.getStartDate());
        dto.setAddressLine1(entity.getAddressLine1());
        dto.setCity(entity.getCity());
        dto.setState(entity.getState());
        dto.setPostalCode(entity.getPostalCode());
        dto.setCountry(entity.getCountry());
        dto.setCreateBy(entity.getCreateBy());
        dto.setEditBy(entity.getEditBy());

        if (entity.getClazz() != null) {
            dto.setClazzId(entity.getClazz().getId());
            dto.setClazzName(entity.getClazz().getName());
        }
        return dto;
    }
}
