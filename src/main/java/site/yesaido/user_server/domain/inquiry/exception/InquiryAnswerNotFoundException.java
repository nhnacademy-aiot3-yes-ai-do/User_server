package site.yesaido.user_server.domain.inquiry.exception;

public class InquiryAnswerNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "존재하지 않는 문의 답변입니다.";
    public InquiryAnswerNotFoundException() { super(DEFAULT_MESSAGE); }
    public InquiryAnswerNotFoundException(String message) { super(message); }
}