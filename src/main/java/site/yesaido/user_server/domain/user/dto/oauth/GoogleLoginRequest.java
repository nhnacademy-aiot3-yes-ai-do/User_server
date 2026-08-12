package site.yesaido.user_server.domain.user.dto.oauth;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "이메일 입력은 필수입니다.")
        String email,

        String nickName
){}
