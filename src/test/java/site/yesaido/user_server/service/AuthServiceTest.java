package site.yesaido.user_server.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import site.yesaido.user_server.domain.user.dto.TokenResponse;
import site.yesaido.user_server.domain.user.dto.login.LoginRequest;
import site.yesaido.user_server.domain.user.entity.Role;
import site.yesaido.user_server.domain.user.entity.User;
import site.yesaido.user_server.domain.user.entity.UserStatus;
import site.yesaido.user_server.domain.user.exception.UserNotFoundException;
import site.yesaido.user_server.domain.user.repository.UserRepository;
import site.yesaido.user_server.domain.user.service.AuthService;
import site.yesaido.user_server.global.jwt.JwtTokenProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("로그인 기능 테스트")
    class LoginTest{
        @Test
        @DisplayName("성공 : 이메일과 비밀번호가 올바르면 토큰 보따리로 반환")
        void success_login(){
            LoginRequest request = createLoginRequest("test@naver.com", "nhn123!");
            User user = createUser(1L, request.getEmail(), request.getPassword());

            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
            given(jwtTokenProvider.createAccessToken(anyLong(), any(), any())).willReturn("mockAccessToken");
            given(jwtTokenProvider.createRefreshToken(anyLong())).willReturn("mockRefreshToken");

            TokenResponse response = authService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("mockAccessToken");
            assertThat(response.getRefreshToken()).isEqualTo("mockRefreshToken");
            assertThat(response.getType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("실패 : 존재하지 않는 이메일로 로그인 시 예외가 발생")
        void login_userNotFound(){
            LoginRequest request = createLoginRequest("test@naver.com", "nhn123!");
            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, ()-> authService.login(request));
        }

        @Test
        @DisplayName("실패 : 비밀번호가 올바르지 않아 예외 발생")
        void login_invalidPassword(){
            LoginRequest request = createLoginRequest("test@naver.com", "nhn123!");
            User user = createUser(1L, "test@naver.com", "123!encodedPassword");

            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);

            assertThrows(IllegalArgumentException.class, ()-> authService.login(request));
        }
    }

    public LoginRequest createLoginRequest(String email, String password){
        LoginRequest dto = new LoginRequest();
        ReflectionTestUtils.setField(dto, "email", email);
        ReflectionTestUtils.setField(dto, "password", password);
        return dto;
    }

    public User createUser(Long id, String email, String password){
        return User.builder()
                .id(id)
                .email(email)
                .password(password)
                .role(Role.USER)
                .nickName("newNick")
                .status(UserStatus.ACTIVE)
                .build();
    }


}
