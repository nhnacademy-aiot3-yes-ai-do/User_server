package site.yesaido.user_server.domain.user.dto.profile;

import jakarta.validation.constraints.NotBlank;

public record PasswordVerifyRequest(
        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        String password
) {
}
