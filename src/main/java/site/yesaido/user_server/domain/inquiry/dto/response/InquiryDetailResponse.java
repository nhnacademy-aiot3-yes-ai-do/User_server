package site.yesaido.user_server.domain.inquiry.dto.response;

import site.yesaido.user_server.domain.inquiry.entity.Inquiry;
import site.yesaido.user_server.domain.inquiry.entity.InquiryAnswer;
import site.yesaido.user_server.domain.inquiry.entity.InquiryStatus;

import java.time.LocalDateTime;
import java.util.List;

public record InquiryDetailResponse(
        Long id,
        Long userId,
        Long categoryId,
        String categoryName,
        String title,
        InquiryStatus status,
        LocalDateTime createdAt,
        Long cultivationId,
        List<InquiryMessageResponse> messages
) {
    public static InquiryDetailResponse of(Inquiry inquiry, List<InquiryAnswer> answer) {
        return new InquiryDetailResponse(
                inquiry.getId(),
                inquiry.getUserId(),
                inquiry.getCategory().getId(),
                inquiry.getCategory().getCategoryName(),
                inquiry.getTitle(),
                inquiry.getStatus(),
                inquiry.getCreatedAt(),
                inquiry.getCultivationId(),
                answer.stream().map(InquiryMessageResponse::from).toList()
        );
    }
}
