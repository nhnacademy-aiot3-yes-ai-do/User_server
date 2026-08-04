package site.yesaido.user_server.domain.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import site.yesaido.user_server.domain.user.dto.token.TokenResponse;
import site.yesaido.user_server.domain.user.dto.login.LoginRequest;
import site.yesaido.user_server.domain.user.entity.Role;
import site.yesaido.user_server.domain.user.entity.User;
import site.yesaido.user_server.domain.user.entity.UserStatus;
import site.yesaido.user_server.domain.user.exception.AlreadyWithdrawnException;
import site.yesaido.user_server.domain.user.exception.InvalidPasswordException;
import site.yesaido.user_server.domain.user.exception.InvalidTokenException;
import site.yesaido.user_server.domain.user.exception.UserNotFoundException;
import site.yesaido.user_server.domain.user.repository.UserRepository;
import site.yesaido.user_server.global.jwt.JwtTokenProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

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
            given(jwtTokenProvider.createAccessToken(anyLong(), any())).willReturn("mockAccessToken");
            given(jwtTokenProvider.createRefreshToken(anyLong())).willReturn("mockRefreshToken");
            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

            TokenResponse response = authService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("mockAccessToken");
            assertThat(response.getRefreshToken()).isEqualTo("mockRefreshToken");

        }

        @Test
        @DisplayName("실패 : 존재하지 않는 이메일로 로그인 시 예외가 발생")
        void login_userNotFound(){
            LoginRequest request = createLoginRequest("test@naver.com", "nhn12345!");
            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, ()-> authService.login(request));
        }

        @Test
        @DisplayName("실패 : 비밀번호가 올바르지 않아 예외 발생")
        void login_invalidPassword(){
            LoginRequest request = createLoginRequest("test@naver.com", "nhn12345!");
            User user = createUser(1L, "test@naver.com", "123!encodedPassword");

            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);

            assertThrows(InvalidPasswordException.class, ()-> authService.login(request));
        }

        @Test
        @DisplayName("실패 : 탈퇴한 사용자로 로그인 시 예외 발생")
        void login_alreadyWithdrawn(){
            LoginRequest request = createLoginRequest("test@naver.com", "nhn12345!");
            User withdrawnUser = User.builder()
                    .id(1L)
                    .email("test@naver.com")
                    .password("nhn12345!")
                    .status(UserStatus.DELETED)
                    .build();

            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(withdrawnUser));

            assertThrows(AlreadyWithdrawnException.class, () -> authService.login(request));

        }
    }

    @Nested
    @DisplayName("토큰 재발급(reissue) 기능 테스트")
    class ReissueTest{
        @Test
        @DisplayName("성공 : 유효한 RefreshToken이면 새로운 토큰 반환")
        void reissue_success() {
            String refreshToken = "validRefreshToken";
            Long userId = 1L;
            User user = createUser(userId, "test@naver.com", "password");

            given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
            given(jwtTokenProvider.getUserIdFromToken(refreshToken)).willReturn(userId);
            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("RT:" + userId)).willReturn(refreshToken);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(jwtTokenProvider.createAccessToken(anyLong(), any())).willReturn("newAccessToken");
            given(jwtTokenProvider.createRefreshToken(anyLong())).willReturn("newRefreshToken");

            TokenResponse response = authService.reissue(refreshToken);

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
            assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");
        }

        @Test
        @DisplayName("실패 : 만료되거나 유효하지 않은 RefreshToken이면 예외 발생")
        void reissue_invalidJwtToken(){
            String invalidToken = "invalidToken";
            given(jwtTokenProvider.validateToken(invalidToken)).willReturn(false);

            assertThrows(InvalidTokenException.class, () -> authService.reissue(invalidToken));
        }

        @Test
        @DisplayName("실패 : 레디스에 저장된 토큰과 다르면 예외 발생")
        void reissue_redisMismatch(){
            String refreshToken = "validToken";
            Long userId = 1L;

            given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
            given(jwtTokenProvider.getUserIdFromToken(refreshToken)).willReturn(userId);
            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("RT:" + userId)).willReturn("differentTokenInRedis");

            assertThrows(InvalidTokenException.class, () -> authService.reissue(refreshToken));

        }
    }

    @Nested
    @DisplayName("로그아웃 기능 테스트")
    class LogoutTest{
        @Test
        @DisplayName("성공 : 로그아웃 시 레디스의 RefreshToken 키 삭제")
        void logout_success(){
            Long userId = 1L;
            authService.logout(userId);

            verify(stringRedisTemplate).delete("RT:"+ userId);
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
