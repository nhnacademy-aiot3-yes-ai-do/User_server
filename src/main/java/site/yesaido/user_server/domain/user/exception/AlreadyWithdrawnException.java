package site.yesaido.user_server.domain.user.exception;

public class AlreadyWithdrawnException extends RuntimeException {
    private static final String MESSAGE = "이미 탈퇴한 회원입니다.";

    public AlreadyWithdrawnException(){
        super(MESSAGE);
    }

    public AlreadyWithdrawnException(String message) {
        super(MESSAGE);
    }
}
