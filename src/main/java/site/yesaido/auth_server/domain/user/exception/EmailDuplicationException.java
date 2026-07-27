package site.yesaido.auth_server.domain.user.exception;

public class EmailDuplicationException extends RuntimeException {
    public EmailDuplicationException(String message) {
        super(message);
    }
}
