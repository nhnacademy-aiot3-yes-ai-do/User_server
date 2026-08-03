package site.yesaido.user_server.domain.email.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import site.yesaido.user_server.domain.email.dto.EmailSendRequest;
import site.yesaido.user_server.domain.email.dto.EmailVerifyRequest;
import site.yesaido.user_server.domain.email.service.EmailService;

import java.net.InetSocketAddress;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class EmailControllerTest {
    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailController emailController;

    @Test
    @DisplayName("성공 : X-Forwarded-For 헤더가 있으면 해당 IP로 발송 요청한다.")
    void sendEmail_success_withForwardedFor(){
        EmailSendRequest request = new EmailSendRequest("test@naver.com");
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/auth/email/send")
                        .header("X-Forwarded-For", "1.2.3.4, 5.6.7.8")
        );

        ResponseEntity<String> response = emailController.sendEmail(request, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("인증번호가 발송되었습니다.");
        verify(emailService).sendVerificationEmail("test@naver.com", "1.2.3.4");
    }

    @Test
    @DisplayName("성공 : X-Forwarded-For 헤더가 없으면 remoteAddress로 발송 요청한다.")
    void sendEmail_success_withoutForwardedFor(){
        EmailSendRequest request = new EmailSendRequest("test@naver.com");
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/auth/email/send")
                        .remoteAddress(new InetSocketAddress("127.0.0.1", 12345))
        );

        ResponseEntity<String> response = emailController.sendEmail(request, exchange);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(emailService).sendVerificationEmail("test@naver.com", "127.0.0.1");
    }

    @Test
    @DisplayName("성공 : 인증번호 검증 요청 시 200 OK와 일치 결과 true를 반환한다.")
    void verifyEmail_success(){
        EmailVerifyRequest request = new EmailVerifyRequest("test@naver.com", "123456");
        given(emailService.verifyCode("test@naver.com", "123456")).willReturn(true);

        ResponseEntity<Boolean> response = emailController.verifyEmail(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
        verify(emailService).verifyCode("test@naver.com", "123456");
    }
}