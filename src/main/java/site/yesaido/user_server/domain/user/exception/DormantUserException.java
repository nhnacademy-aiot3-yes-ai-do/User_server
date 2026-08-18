package site.yesaido.user_server.domain.user.exception;

public class DormantUserException extends RuntimeException {
    private static final String MESSAGE = "휴먼 처리된 계정입니다.";

    public DormantUserException(){
        super(MESSAGE);
    }

    public DormantUserException(String message) {
        super(message);
    }
}
