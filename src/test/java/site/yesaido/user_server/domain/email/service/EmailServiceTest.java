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
import site.yesaido.user_server.domain.user.exception.TooManyRequestException;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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
        @DisplayName("성공 : 이메일 발송 및 레디스 5분 TTL 저장, 60초 쿨다운 등록")
        void success_sendVerificationEmail(){
            String toEmail = "test@naver.com";
            String clientIp = "1.2.3.4";
            given(stringRedisTemplate.hasKey("EMAIL_SEND_COOLDOWN:" + toEmail)).willReturn(false);
            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.increment("EMAIL_SEND_IP:" + clientIp)).willReturn(1L);

            emailService.sendVerificationEmail(toEmail, clientIp);

            verify(mailSender).send(any(SimpleMailMessage.class));
            verify(stringRedisTemplate).expire("EMAIL_SEND_IP:" + clientIp, 3600L, TimeUnit.SECONDS);
            verify(valueOperations).set(eq("EMAIL_VERIFY:" + toEmail), anyString(), eq(5L), eq(TimeUnit.MINUTES));
            verify(valueOperations).set(eq("EMAIL_SEND_COOLDOWN:" + toEmail), eq("1"), eq(60L), eq(TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("실패 : 쿨다운(60초) 내 재요청 시 TooManyRequestException")
        void fail_sendVerificationEmail_cooldownActive(){
            String toEmail = "test@naver.com";
            String clientIp = "1.2.3.4";
            given(stringRedisTemplate.hasKey("EMAIL_SEND_COOLDOWN:" + toEmail)).willReturn(true);

            assertThatThrownBy(() -> emailService.sendVerificationEmail(toEmail, clientIp))
                    .isInstanceOf(TooManyRequestException.class);

            verify(mailSender, never()).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("실패 : 동일 IP 시간당 발송 한도 초과 시 TooManyRequestException")
        void fail_sendVerificationEmail_ipLimitExceeded(){
            String toEmail = "test@naver.com";
            String clientIp = "1.2.3.4";
            given(stringRedisTemplate.hasKey("EMAIL_SEND_COOLDOWN:" + toEmail)).willReturn(false);
            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.increment("EMAIL_SEND_IP:" + clientIp)).willReturn(6L);

            assertThatThrownBy(() -> emailService.sendVerificationEmail(toEmail, clientIp))
                    .isInstanceOf(TooManyRequestException.class);

            verify(mailSender, never()).send(any(SimpleMailMessage.class));
        }
    }

    @Nested
    @DisplayName("인증번호 검증 테스트")
    class VerifyCodeTest{
        @Test
        @DisplayName("성공 : 인증번호가 일치하면 true 반환, 코드/실패카운트 레디스 키 삭제")
        void success_verifyCode(){
            String email = "test@naver.com";
            String code = "123456";

            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("EMAIL_VERIFY_FAIL:" + email)).willReturn(null);
            given(valueOperations.get("EMAIL_VERIFY:" + email)).willReturn("123456");

            boolean result = emailService.verifyCode(email, code);

            assertThat(result).isTrue();
            verify(stringRedisTemplate).delete("EMAIL_VERIFY:" + email);
            verify(stringRedisTemplate).delete("EMAIL_VERIFY_FAIL:" + email);
        }

        @Test
        @DisplayName("실패 : 레디스에 저장된 인증번호가 없으면 false 반환 및 실패카운트 증가")
        void fail_verifyCode_expired(){
            String email = "test@naver.com";
            String code = "123456";

            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("EMAIL_VERIFY_FAIL:" + email)).willReturn(null);
            given(valueOperations.get("EMAIL_VERIFY:" + email)).willReturn(null);
            given(valueOperations.increment("EMAIL_VERIFY_FAIL:" + email)).willReturn(1L);

            boolean result = emailService.verifyCode(email, code);

            assertThat(result).isFalse();
            verify(stringRedisTemplate).expire("EMAIL_VERIFY_FAIL:" + email, 5L, TimeUnit.MINUTES);
        }

        @Test
        @DisplayName("실패 : 인증번호가 일치하지 않으면 false 반환 및 실패카운트 증가")
        void fail_verifyCode_mismatch(){
            String email = "test@naver.com";
            String inputCode = "123456";
            String savedCode = "654321";

            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("EMAIL_VERIFY_FAIL:" + email)).willReturn(null);
            given(valueOperations.get("EMAIL_VERIFY:" + email)).willReturn(savedCode);
            given(valueOperations.increment("EMAIL_VERIFY_FAIL:" + email)).willReturn(1L);

            boolean result = emailService.verifyCode(email, inputCode);

            assertThat(result).isFalse();
            verify(valueOperations).increment("EMAIL_VERIFY_FAIL:" + email);
        }

        @Test
        @DisplayName("실패 : 연속 실패 5회 초과 시 TooManyRequestsException, 코드 비교 자체를 시도하지 않음")
        void fail_verifyCode_tooManyAttempts(){
            String email = "test@naver.com";
            String code = "123456";

            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("EMAIL_VERIFY_FAIL:" + email)).willReturn("5");

            assertThatThrownBy(() -> emailService.verifyCode(email, code))
                    .isInstanceOf(TooManyRequestException.class);

            verify(valueOperations, never()).get("EMAIL_VERIFY:" + email);
        }
    }
}