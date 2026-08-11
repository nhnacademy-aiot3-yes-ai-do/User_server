package site.yesaido.user_server.domain.inquiry;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import site.yesaido.user_server.domain.inquiry.entity.Inquiry;
import site.yesaido.user_server.domain.inquiry.entity.InquiryAnswer;
import site.yesaido.user_server.domain.inquiry.entity.InquiryCategory;
import site.yesaido.user_server.domain.inquiry.entity.InquiryStatus;
import site.yesaido.user_server.domain.inquiry.exception.InquiryAnswerThreadMismatchException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InquiryTest {
    @Test
    @DisplayName("Inquiry 카테고리 이름 변경 테스트")
    void inquiryCategoryUpdateTest() {
        InquiryCategory category = InquiryCategory.builder()
                .id(1L)
                .categoryName("기타 문의")
                .build();

        category.updateCategoryName("시스템 오류 문의");

        assertThat(category.getCategoryName()).isEqualTo("시스템 오류 문의");
    }

    @Test
    @DisplayName("Inquiry 상태 변경 테스트 (markResolved, markPending)")
    void inquiryStatusTest() {
        InquiryCategory category = InquiryCategory.builder().id(1L).categoryName("일반").build();
        Inquiry inquiry = Inquiry.builder()
                .id(100L)
                .userId(1L)
                .title("문의 제목")
                .category(category)
                .build();

        assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.PENDING);

        inquiry.markResolved();
        assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.RESOLVED);

        inquiry.markPending();
        assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.PENDING);
    }

    @Test
    @DisplayName("InquiryAnswer 최초 답변 생성 및 답변 작성 테스트")
    void inquiryAnswerRootTest() {
        Inquiry inquiry = Inquiry.builder().id(100L).build();
        InquiryAnswer root = InquiryAnswer.createRoot(inquiry, "최초 질문 내용");

        assertThat(root.getInquiry()).isEqualTo(inquiry);
        assertThat(root.getContent()).isEqualTo("최초 질문 내용");

        root.answer("관리자 답변 내용");
        assertThat(root.getAnswerContent()).isEqualTo("관리자 답변 내용");
    }

    @Test
    @DisplayName("InquiryAnswer 스레드 불일치 시 InquiryAnswerThreadMismatchException 발생 테스트")
    void inquiryAnswerMismatchExceptionTest() {
        Inquiry inquiry1 = Inquiry.builder().id(1L).build();
        Inquiry inquiry2 = Inquiry.builder().id(2L).build();

        InquiryAnswer preAnswer = InquiryAnswer.builder().id(10L).inquiry(inquiry1).build();

        assertThatThrownBy(() -> InquiryAnswer.createFollowUp(inquiry2, preAnswer, "추가 질문"))
                .isInstanceOf(InquiryAnswerThreadMismatchException.class)
                .hasMessageContaining("다른 문의");
    }

}
