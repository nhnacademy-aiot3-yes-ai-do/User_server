package site.yesaido.user_server.domain.user.dto.profile;

import site.yesaido.user_server.domain.user.entity.Role;
import site.yesaido.user_server.domain.user.entity.User;
import site.yesaido.user_server.domain.user.entity.UserStatus;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String email,
        String nickname,
        Role role,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserProfileResponse from(User user){
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
