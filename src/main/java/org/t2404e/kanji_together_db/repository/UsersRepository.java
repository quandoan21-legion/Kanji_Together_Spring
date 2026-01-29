package org.t2404e.kanji_together_db.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.t2404e.kanji_together_db.entity.Users;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    Optional<Users> findByUsername(String username);
    // Lưu ý: Đổi "User" thành "Users" để khớp với tên Class Entity của bạn
    @Query("SELECT u FROM Users u WHERE " +
            "(:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:active IS NULL OR u.isActive = :active)")
    Page<Users> findByFilters(@Param("name") String name,
                              @Param("email") String email,
                              @Param("active") Boolean active,
                              Pageable pageable);
}