package site.yesaido.user_server.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import site.yesaido.user_server.domain.inquiry.exception.InquiryAccessDeniedException;
import site.yesaido.user_server.domain.inquiry.exception.InquiryAnswerNotFoundException;
import site.yesaido.user_server.domain.inquiry.exception.InquiryCategoryNotFoundException;
import site.yesaido.user_server.domain.inquiry.exception.InquiryNotFoundException;
import site.yesaido.user_server.domain.user.exception.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 404
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e, ServerWebExchange exchange){
        logWarnFormat(HttpStatus.NOT_FOUND, e, exchange);
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({
            InquiryNotFoundException.class,
            InquiryCategoryNotFoundException.class,
            InquiryAnswerNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleInquiryNotFoundException(Exception e, ServerWebExchange exchange){
        logWarnFormat(HttpStatus.NOT_FOUND, e, exchange);
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 400
    @ExceptionHandler({
            NicknameDuplicationException.class,
            EmailDuplicationException.class,
            InvalidPasswordException.class,
            AlreadyWithdrawnException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestException(RuntimeException e, ServerWebExchange exchange){
        logWarnFormat(HttpStatus.BAD_REQUEST, e, exchange);
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 401
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException e, ServerWebExchange exchange){
        logWarnFormat(HttpStatus.UNAUTHORIZED, e, exchange);
        return buildResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(TooManyRequestException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequestException(TooManyRequestException e, ServerWebExchange exchange) {
        logWarnFormat(HttpStatus.TOO_MANY_REQUESTS, e, exchange);
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
    }

    // 403
    @ExceptionHandler(InquiryAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleInquiryAccessDeniedException(InquiryAccessDeniedException e, ServerWebExchange exchange){
        logWarnFormat(HttpStatus.FORBIDDEN, e, exchange);
        return buildResponse(HttpStatus.FORBIDDEN, e.getMessage());
    }

    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e, ServerWebExchange exchange){
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();

        log.error("ERROR [서버 장애 발생] API: {} {} | 예외: {} | 상세원인: {}",
                method, path, e.getClass().getSimpleName(), e.getMessage());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다");
    }


    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleWebExchangeBindException(WebExchangeBindException e, ServerWebExchange exchange){
        String errorMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        logWarnFormat(HttpStatus.BAD_REQUEST, e, exchange);
        return buildResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    private void logWarnFormat(HttpStatus status, Exception e, ServerWebExchange exchange){
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();

        log.warn("WARN [요청 예외 발생] API: {} {} | 상태: {} | 예외: {} | 상세원인: {}",
                method, path, status.value(), e.getClass().getSimpleName(), e.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message){
        ErrorResponse response = ErrorResponse.builder()
                .status(status.value())
                .error(status.name())
                .message(message)
                .build();
        return ResponseEntity.status(status).body(response);
    }

}
