package site.yesaido.auth_server.domain.user.exception;

public class NicknameDuplicationException extends RuntimeException {
    public NicknameDuplicationException(String message) {
        super(message);
    }
}
