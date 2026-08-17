package site.yesaido.user_server.domain.inquiry.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryCreateRequest {
    @NotNull(message = "카테고리는 필수 입력 항목입니다.")
    private Long categoryId;

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
    private String title;

    @NotBlank(message = "문의 내역은 필수 입력 항목입니다.")
    private String content;

    private Long cultivationId;
}
