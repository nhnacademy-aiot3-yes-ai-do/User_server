package site.yesaido.user_server.domain.user.exception;

public class InvalidTokenException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "유효하지 않거나 만료된 토큰입니다.";

    public InvalidTokenException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidTokenException(String message) {
        super(message);
    }
}
