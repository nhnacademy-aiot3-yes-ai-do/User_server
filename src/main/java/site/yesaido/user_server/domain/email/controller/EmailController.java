package site.yesaido.user_server.domain.email.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import site.yesaido.user_server.domain.email.dto.EmailSendRequest;
import site.yesaido.user_server.domain.email.dto.EmailVerifyRequest;
import site.yesaido.user_server.domain.email.service.EmailService;

@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@Valid @RequestBody EmailSendRequest request, ServerWebExchange exchange) {
        emailService.sendVerificationEmail(request.getEmail(), resolveClientIp(exchange));
        return ResponseEntity.ok("인증번호가 발송되었습니다.");
    }

    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyEmail(@Valid @RequestBody EmailVerifyRequest request){
        boolean verifyCode = emailService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(verifyCode);
    }

    // Helper Method
    private String resolveClientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }
}
