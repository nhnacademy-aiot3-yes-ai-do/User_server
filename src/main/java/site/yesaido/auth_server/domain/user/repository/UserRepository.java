package site.yesaido.auth_server.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.yesaido.auth_server.domain.user.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 로그인할 떄 이메일로 유저 찾기
    Optional<User> findByEmail(String email);
    // 이메일 중복 확인
    boolean existsByEmail(String email);
    // 닉네임 중복 확인
    boolean existsByNickName(String nickname);
}
