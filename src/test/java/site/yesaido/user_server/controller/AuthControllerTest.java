package site.yesaido.user_server.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import site.yesaido.user_server.domain.user.controller.AuthController;
import site.yesaido.user_server.domain.user.dto.TokenResponse;
import site.yesaido.user_server.domain.user.dto.login.LoginRequest;
import site.yesaido.user_server.domain.user.service.AuthService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("로그인 성공 시 200과 토큰 응답을 반환한다")
    void login_success() {
        LoginRequest request = new LoginRequest();
        TokenResponse tokenResponse = TokenResponse.builder()
                .type("Bearer")
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expireInl(1800L)
                .build();

        given(authService.login(request)).willReturn(tokenResponse);

        ResponseEntity<TokenResponse> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccessToken()).isEqualTo("access-token");
        assertThat(response.getBody().getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getBody().getType()).isEqualTo("Bearer");
    }
}