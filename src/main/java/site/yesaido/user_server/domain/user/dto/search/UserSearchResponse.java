package site.yesaido.user_server.domain.user.dto.search;

import site.yesaido.user_server.domain.user.entity.User;

public record UserSearchResponse(
        Long userId,
        String nickname
) {
    public static UserSearchResponse from(User user) {
        return new UserSearchResponse(user.getId(), user.getNickName());
    }
}
