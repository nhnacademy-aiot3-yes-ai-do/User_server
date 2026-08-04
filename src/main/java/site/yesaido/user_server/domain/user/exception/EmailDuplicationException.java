package site.yesaido.user_server.domain.user.exception;

public class EmailDuplicationException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "이미 사용 중인 이메일입니다.";

    public EmailDuplicationException() {
        super(DEFAULT_MESSAGE);
    }

    public EmailDuplicationException(String message) {
        super(message);
    }
}
