package site.yesaido.user_server.domain.user.exception;

public class AlreadyWithdrawnException extends RuntimeException {
    public AlreadyWithdrawnException(String message) {
        super(message);
    }
}
