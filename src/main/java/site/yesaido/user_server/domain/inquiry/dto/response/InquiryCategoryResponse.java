package site.yesaido.user_server.domain.inquiry.dto.response;

import site.yesaido.user_server.domain.inquiry.entity.InquiryCategory;

public record InquiryCategoryResponse(Long id, String categoryName) {
    public static InquiryCategoryResponse from(InquiryCategory inquiryCategory) {
        return new InquiryCategoryResponse(inquiryCategory.getId(), inquiryCategory.getCategoryName());
    }
}
