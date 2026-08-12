package site.yesaido.user_server.domain.user.dto.oauth;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "idToken은 필수입니다.")
        String idToken,

        String email,
        String nickName
){}
