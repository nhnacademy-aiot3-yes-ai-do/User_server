package site.yesaido.user_server.domain.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private static final String PREFIX = "EMAIL_VERIFY:";

    private final JavaMailSender mailSender;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail){
        String authCode = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        log.info("이메일 인증번호 발송 수신자 : {}, 인증번호 : {}", toEmail, authCode);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[MushMush] 회원가입 이메일 인증번호입니다.");
        message.setText("안녕하세요! 회원가입 인증번호는 [" + authCode + "] 입니다.\n5분 이내에 입력해 주세요.");

        mailSender.send(message);
        stringRedisTemplate.opsForValue().set(PREFIX + toEmail, authCode, 5, TimeUnit.MINUTES);
        log.info("이메일 인증번호 발송 완료: email={}, code={}", toEmail, authCode);
    }

    public boolean verifyCode(String email, String inputCode){
        String savedCode = stringRedisTemplate.opsForValue().get(PREFIX + email);

        if(savedCode == null || !savedCode.equals(inputCode)){
            return false;
        }

        stringRedisTemplate.delete(PREFIX + email);
        return true;
    }

}
