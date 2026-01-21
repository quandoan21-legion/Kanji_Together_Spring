package org.t2404e.kanji_together_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.t2404e.kanji_together_db.entity.UserEmailOtp;
import org.t2404e.kanji_together_db.entity.Users;

import java.util.Optional;

@Repository
public interface UserEmailOtpRepository extends JpaRepository<UserEmailOtp, Long> {
    Optional<UserEmailOtp> findTopByUserOrderByIdDesc(Users user);
}
