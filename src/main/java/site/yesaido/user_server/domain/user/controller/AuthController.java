package site.yesaido.user_server.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.yesaido.user_server.domain.user.dto.token.ReissueRequest;
import site.yesaido.user_server.domain.user.dto.token.TokenResponse;
import site.yesaido.user_server.domain.user.dto.login.LoginRequest;
import site.yesaido.user_server.domain.user.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request){
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@Valid @RequestBody ReissueRequest reissueRequest){
        TokenResponse response = authService.reissue(reissueRequest.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("X-User-Id") Long userId){
        authService.logout(userId);
        return ResponseEntity.ok().build();
    }

}
