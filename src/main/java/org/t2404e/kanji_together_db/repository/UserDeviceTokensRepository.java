package org.t2404e.kanji_together_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.t2404e.kanji_together_db.entity.UserDeviceTokens;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserDeviceTokensRepository extends JpaRepository<UserDeviceTokens, Long> {
    List<UserDeviceTokens> findByUser_IdInAndIsActiveTrue(Collection<Long> userIds);

    List<UserDeviceTokens> findByUser_Id(Long userId);

    Optional<UserDeviceTokens> findByUser_IdAndToken(Long userId, String token);

    Optional<UserDeviceTokens> findByToken(String token);
}
