package site.yesaido.user_server.domain.inquiry.dto.response;

import site.yesaido.user_server.domain.inquiry.entity.InquiryAnswer;

import java.time.LocalDateTime;

public record InquiryMessageResponse(
        Long id,
        Long preId,
        String content,
        String answerContent,
        LocalDateTime createdAt
) {
    public static InquiryMessageResponse from(InquiryAnswer answer) {
        return new InquiryMessageResponse(
                answer.getId(),
                answer.getPre() != null ? answer.getPre().getId() : null,
                answer.getContent(),
                answer.getAnswerContent(),
                answer.getCreatedAt()
        );
    }
}
