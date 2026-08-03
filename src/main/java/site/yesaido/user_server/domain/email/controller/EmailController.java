package site.yesaido.user_server.domain.email.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.yesaido.user_server.domain.email.dto.EmailSendRequest;
import site.yesaido.user_server.domain.email.dto.EmailVerifyRequest;
import site.yesaido.user_server.domain.email.service.EmailService;

@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@Valid @RequestBody EmailSendRequest request){
        emailService.sendVerificationEmail(request.getEmail());
        return ResponseEntity.ok("인증번호가 발송되었습니다.");
    }

    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyEmail(@Valid @RequestBody EmailVerifyRequest request){
        boolean verifyCode = emailService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(verifyCode);
    }
}
