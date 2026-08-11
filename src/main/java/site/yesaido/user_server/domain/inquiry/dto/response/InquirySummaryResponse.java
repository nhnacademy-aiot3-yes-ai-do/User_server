package site.yesaido.user_server.domain.inquiry.dto.response;

import site.yesaido.user_server.domain.inquiry.entity.Inquiry;
import site.yesaido.user_server.domain.inquiry.entity.InquiryStatus;

import java.time.LocalDateTime;

public record InquirySummaryResponse(
        Long id,
        Long userId,
        String categoryName,
        String title,
        InquiryStatus status,
        LocalDateTime createdAt
) {
    public static InquirySummaryResponse from(Inquiry inquiry) {
        return new InquirySummaryResponse(
                inquiry.getId(),
                inquiry.getUserId(),
                inquiry.getCategory().getCategoryName(),
                inquiry.getTitle(),
                inquiry.getStatus(),
                inquiry.getCreatedAt()
        );
    }
}
