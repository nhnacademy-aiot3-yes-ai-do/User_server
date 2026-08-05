package site.yesaido.user_server.domain.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncMailSender {

    private final JavaMailSender mailSender;

    @Async
    public void sendMailAsync(SimpleMailMessage message){
        try{
            mailSender.send(message);
            log.info("이메일 발송 완료 : {}", (Object) message.getTo());
        }catch (Exception e){
            log.info("이메일 발송 실패 : {}", e.getMessage());
        }
    }
}
