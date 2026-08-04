package site.yesaido.user_server.global.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import site.yesaido.user_server.domain.user.exception.*;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    private void setupMockExchange(){
        given(exchange.getRequest()).willReturn(request);
        given(request.getMethod()).willReturn(HttpMethod.POST);
        given(request.getURI()).willReturn(URI.create("http://localhost:8081/api/auth/login"));
    }

    @Test
    @DisplayName("UserNotFoundException 발생 시 404 NOT_FOUND와 에러 메시지를 반환한다")
    void handleUserNotFoundException_success(){
        setupMockExchange();
        UserNotFoundException e = new UserNotFoundException("존재하지 않는 사용자입니다.");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUserNotFoundException(e, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("InvalidPasswordException 발생 시 400 BAD_REQUEST와 에러 메시지를 반환한다")
    void handleInvalidPasswordException_success(){
        setupMockExchange();
        InvalidPasswordException e = new InvalidPasswordException();

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBadRequestException(e, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("EmailDuplicationException 발생 시 400 BAD_REQUEST와 에러 메시지를 반환한다")
    void handleEmailDuplicationException_success(){
        setupMockExchange();
        EmailDuplicationException e = new EmailDuplicationException("이메일이 중복됩니다.");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBadRequestException(e, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("이메일이 중복됩니다.");
    }

    @Test
    @DisplayName("NicknameDuplicationException 발생 시 400 BAD_REQUEST와 에러 메시지를 반환한다")
    void handleNicknameDuplicationException_success(){
        setupMockExchange();
        NicknameDuplicationException e = new NicknameDuplicationException("닉네임이 중복됩니다.");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBadRequestException(e, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("닉네임이 중복됩니다.");
    }

    @Test
    @DisplayName("AlreadyWithdrawnException 발생 시 400 BAD_REQUEST와 에러 메시지를 반환한다")
    void handleAlreadyWithdrawnException_success(){
        setupMockExchange();
        AlreadyWithdrawnException e = new AlreadyWithdrawnException("이미 탈퇴한 회원입니다.");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBadRequestException(e, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("이미 탈퇴한 회원입니다.");
    }

    @Test
    @DisplayName("InvalidTokenException 발생 시 400 BAD_REQUEST와 에러 메시지를 반환한다")
    void handleInvalidTokenException_success(){
        setupMockExchange();
        InvalidTokenException e = new InvalidTokenException("유효하지 않은 토큰입니다.");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidTokenException(e, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("유효하지 않은 토큰입니다.");
    }

    @Test
    @DisplayName("일반 Exception 발생 시 500 INTERNAL_SERVER_ERROR와 서버 내부 오류 메시지를 반환한다")
    void handleGeneralException_success() {
        setupMockExchange();
        Exception e = new RuntimeException("예측 실패 에러");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGeneralException(e, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("서버 내부 오류가 발생했습니다");
    }

}
