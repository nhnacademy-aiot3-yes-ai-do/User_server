package site.yesaido.user_server.domain.email.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import site.yesaido.user_server.domain.email.dto.EmailSendRequest;
import site.yesaido.user_server.domain.email.dto.EmailVerifyRequest;
import site.yesaido.user_server.domain.email.service.EmailService;
import site.yesaido.user_server.global.common.ApiResponse;
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
    @DisplayName("성공 : 이메일 인증번호 발송 요청 시 200 OK와 성공 메시지를 반환한다")
    void sendEmail_success() {

        EmailSendRequest request = new EmailSendRequest("test@naver.com");

        ResponseEntity<ApiResponse<Void>> response = emailController.sendEmail(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("인증번호가 발송되었습니다.");

        verify(emailService).sendVerificationEmail("test@naver.com");
    }



    @Test
    @DisplayName("성공 : 인증번호 검증 요청 시 200 OK와 일치 결과 true를 반환한다.")
    void verifyEmail_success(){
        EmailVerifyRequest request = new EmailVerifyRequest("test@naver.com", "123456");
        given(emailService.verifyCode("test@naver.com", "123456")).willReturn(true);

        ResponseEntity<ApiResponse<Boolean>> response = emailController.verifyEmail(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).isEqualTo("인증번호 검증 결과입니다.");
        assertThat(response.getBody().getData()).isTrue();

        verify(emailService).verifyCode("test@naver.com", "123456");
    }
}