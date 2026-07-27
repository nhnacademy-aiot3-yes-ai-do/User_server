package site.yesaido.user_server.domain.user.exception;

public class NicknameDuplicationException extends RuntimeException {
    public NicknameDuplicationException(String message) {
        super(message);
    }
}
