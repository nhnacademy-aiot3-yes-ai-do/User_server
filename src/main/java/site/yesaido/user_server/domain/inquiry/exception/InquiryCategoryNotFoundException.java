package site.yesaido.user_server.domain.inquiry.exception;

public class InquiryCategoryNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "존재하지 않는 문의 카테고리입니다.";
    public InquiryCategoryNotFoundException() { super(DEFAULT_MESSAGE); }
    public InquiryCategoryNotFoundException(String message) {
        super(message);
    }
}
