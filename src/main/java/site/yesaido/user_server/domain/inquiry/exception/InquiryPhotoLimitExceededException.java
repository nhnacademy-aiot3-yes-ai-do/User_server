package site.yesaido.user_server.domain.inquiry.exception;

public class InquiryPhotoLimitExceededException extends RuntimeException {
    public InquiryPhotoLimitExceededException(String message) {
        super(message);
    }
}
