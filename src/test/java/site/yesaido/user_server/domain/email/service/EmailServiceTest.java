package site.yesaido.user_server.domain.email.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp(){
        ReflectionTestUtils.setField(emailService, "fromEmail", "mush_official@naver.com");
    }

    @Nested
    @DisplayName("이메일 인증번호 발송 테스트")
    class SendVerificationEmailTest{
        @Test
        @DisplayName("성공 : 이메일 발송 및 레디스 5분 TTL 저장")
        void success_sendVerificationEmail(){
            String toEmail = "test@naver.com";
            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

            emailService.sendVerificationEmail(toEmail);

            verify(mailSender).send(any(SimpleMailMessage.class));
            verify(valueOperations).set(eq("EMAIL_VERIFY:" + toEmail), anyString(), eq(5L), eq(TimeUnit.MINUTES));

        }
    }

    @Nested
    @DisplayName("성공 : 인증번호 검증 테스트")
    class VerifyCodeTest{
        @Test
        @DisplayName("성공 : 인증번호가 일치하면 true 반환 및 레디스 일회용 키 삭제")
        void success_verifyCode(){
            String email = "test@naver.com";
            String code = "123456";

            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("EMAIL_VERIFY:" + email)).willReturn("123456");

            boolean result = emailService.verifyCode(email, code);

            assertThat(result).isTrue();
            verify(stringRedisTemplate).delete("EMAIL_VERIFY:" + email);

        }

        @Test
        @DisplayName("실패 : 레디스에 저장된 인증번호가 없으면 false 반환")
        void fail_verifyCode_expired(){
            String email = "test@naver.com";
            String code = "123456";

            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("EMAIL_VERIFY:" + email)).willReturn(null);

            boolean result = emailService.verifyCode(email, code);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패 : 인증번호가 일치하지 않으면 false 반환")
        void fail_verifyCode_mismatch(){
            String email = "test@naver.com";
            String inputCode = "123456";
            String savedCode = "654321";

            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("EMAIL_VERIFY:" + email)).willReturn(savedCode);

            boolean result = emailService.verifyCode(email, inputCode);

            assertThat(result).isFalse();
        }
    }
}
