package site.yesaido.user_server.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.yesaido.user_server.domain.user.entity.ProfileImage;

import java.util.Optional;

@Repository
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
    Optional<ProfileImage> findByUserId(Long userId);
}
