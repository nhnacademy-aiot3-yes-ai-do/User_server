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
import site.yesaido.user_server.global.common.ApiResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request){
        TokenResponse response = authService.login(request);
        ApiResponse<TokenResponse> apiResponse = ApiResponse.ok("로그인에 성공하였습니다", response);
        return ResponseEntity.status(apiResponse.httpStatus()).body(apiResponse);
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(@Valid @RequestBody ReissueRequest reissueRequest){
        TokenResponse response = authService.reissue(reissueRequest.getRefreshToken());
        ApiResponse<TokenResponse> apiResponse = ApiResponse.ok("토큰이 성공적으로 재발급되었습니다.", response);
        return ResponseEntity.status(apiResponse.httpStatus()).body(apiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("X-User-Id") Long userId){
        authService.logout(userId);
        return ResponseEntity.noContent().build();
    }

}
