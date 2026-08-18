package site.yesaido.user_server.domain.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InquiryCategoryCreateRequest(
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        @Size(max = 100, message = "카테고리 이름은 100자 이하로 입력해주세요.")
        String categoryName
) {
}
