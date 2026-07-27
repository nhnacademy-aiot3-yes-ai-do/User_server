package site.yesaido.auth_server.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {
    private String type;
    private String accessToken;
    private String refreshToken;
    private Long expireInl;
}
