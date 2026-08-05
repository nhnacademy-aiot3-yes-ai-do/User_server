package site.yesaido.user_server.global.exception;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final LocalDateTime dateTime = LocalDateTime.now();
    private final int status;
    private final String error;
    private final String message;
}
