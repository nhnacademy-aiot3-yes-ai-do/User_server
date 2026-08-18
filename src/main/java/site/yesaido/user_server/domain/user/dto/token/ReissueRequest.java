package site.yesaido.user_server.domain.user.dto.token;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReissueRequest {
    @NotBlank(message = "RefreshToken은 필수입니다.")
    private String refreshToken;
}
