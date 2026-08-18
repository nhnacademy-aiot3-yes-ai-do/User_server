package site.yesaido.user_server.domain.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryMessageRequest {
    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    private String content;
}
