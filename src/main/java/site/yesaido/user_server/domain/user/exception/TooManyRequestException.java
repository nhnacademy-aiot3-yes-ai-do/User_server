package site.yesaido.user_server.domain.user.exception;

public class TooManyRequestException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "요청 시도 횟수를 초과했습니다. 잠시 후 다시 시도해 주세요.";

    public TooManyRequestException() {
        super(DEFAULT_MESSAGE);
    }

    public TooManyRequestException(String message) {
        super(message);
    }
}