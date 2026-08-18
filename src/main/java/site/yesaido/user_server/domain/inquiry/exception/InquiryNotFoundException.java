package site.yesaido.user_server.domain.inquiry.exception;

public class InquiryNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "존재하지 않는 문의입니다.";
    public InquiryNotFoundException() { super(DEFAULT_MESSAGE); }
    public InquiryNotFoundException(String message) {
        super(message);
    }
}
