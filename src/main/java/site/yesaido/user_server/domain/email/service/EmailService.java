package site.yesaido.user_server.domain.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import site.yesaido.user_server.domain.user.exception.TooManyRequestException;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private static final String CODE_PREFIX = "EMAIL_VERIFY:";
    private static final String SEND_COOLDOWN_PREFIX = "EMAIL_SEND_COOLDOWN:";
    private static final String SEND_IP_COUNT_PREFIX = "EMAIL_SEND_IP:";
    private static final String VERIFY_FAIL_PREFIX = "EMAIL_VERIFY_FAIL:";

    private static final long SEND_COOLDOWN_SECONDS = 60;
    private static final long SEND_IP_WINDOW_SECONDS = 3600;
    private static final long MAX_SEND_PER_IP_PER_HOUR = 5;
    private static final long MAX_VERIFY_FAIL_COUNT = 5;
    private static final long VERIFY_FAIL_WINDOW_MINUTES = 5;

    private final JavaMailSender mailSender;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String clientIp){
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(SEND_COOLDOWN_PREFIX + toEmail))) {
            throw new TooManyRequestException("잠시 후 다시 시도해주세요. (60초)");
        }
        Long ipCount = stringRedisTemplate.opsForValue().increment(SEND_IP_COUNT_PREFIX + clientIp);

        if (ipCount != null && ipCount == 1L) {
            stringRedisTemplate.expire(SEND_IP_COUNT_PREFIX + clientIp, SEND_IP_WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        if (ipCount != null && ipCount > MAX_SEND_PER_IP_PER_HOUR) {
            throw new TooManyRequestException("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
        }

        String authCode = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        log.info("이메일 인증번호 발송 수신자 : {}", toEmail);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[MushMush] 회원가입 이메일 인증번호입니다.");
        message.setText("안녕하세요! 회원가입 인증번호는 [" + authCode + "] 입니다.\n5분 이내에 입력해 주세요.");

        mailSender.send(message);
        stringRedisTemplate.opsForValue().set(CODE_PREFIX + toEmail, authCode, 5, TimeUnit.MINUTES);
        stringRedisTemplate.opsForValue().set(SEND_COOLDOWN_PREFIX + toEmail, "1", SEND_COOLDOWN_SECONDS, TimeUnit.SECONDS);
        log.info("이메일 인증번호 발송 완료: email={}", toEmail);
    }

    public boolean verifyCode(String email, String inputCode){
        String failCountStr = stringRedisTemplate.opsForValue().get(VERIFY_FAIL_PREFIX + email);
        long failCount = failCountStr == null ? 0 : Long.parseLong(failCountStr);
        if (failCount >= MAX_VERIFY_FAIL_COUNT) {
            throw new TooManyRequestException("인증 시도 횟수를 초과했습니다. 잠시 후 다시 시도해주세요.");
        }

        String savedCode = stringRedisTemplate.opsForValue().get(CODE_PREFIX + email);

        if(savedCode == null || !savedCode.equals(inputCode)){
            Long newFailCount = stringRedisTemplate.opsForValue().increment(VERIFY_FAIL_PREFIX + email);
            if (newFailCount != null && newFailCount == 1L) {
                stringRedisTemplate.expire(VERIFY_FAIL_PREFIX + email, VERIFY_FAIL_WINDOW_MINUTES, TimeUnit.MINUTES);
            }
            return false;
        }

        stringRedisTemplate.delete(CODE_PREFIX + email);
        stringRedisTemplate.delete(VERIFY_FAIL_PREFIX + email);
        return true;
    }

}
