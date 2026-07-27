//package site.yesaido.auth_server.domain.user.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//import java.util.Random;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class EmailService {
////    private final JavaMailSender javaMailSender;
////
////    public String sendVerificationCode(String toEmail){
////        String authCode = createCode();
////
////        SimpleMailMessage message = new SimpleMailMessage();
////        message.setTo(toEmail);
////        message.setSubject("회원가입 인증 번호");
////        message.setText("인증 번호를 입력해주세요: " + authCode);
////
////        javaMailSender.send(message);
////
////        return authCode;
////    }
////
////    public String createCode(){
////        Random random = new Random();
////        StringBuilder key = new StringBuilder();
////        for(int i = 0; i<6; i++){
////            key.append(random.nextInt(10));
////        }
////        return key.toString();
////    }
//}
