package site.yesaido.user_server.domain.user.dto;

import site.yesaido.user_server.domain.user.entity.User;

public record UserSummaryResponse(Long userId, String nickname) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getNickName());
    }
}
