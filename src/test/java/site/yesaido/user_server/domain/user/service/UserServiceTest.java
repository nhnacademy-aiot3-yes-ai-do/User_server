package site.yesaido.user_server.domain.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import site.yesaido.user_server.domain.user.dto.UserSummaryResponse;
import site.yesaido.user_server.domain.user.dto.search.UserSearchResponse;
import site.yesaido.user_server.domain.user.dto.signup.UserSignResponse;
import site.yesaido.user_server.domain.user.dto.signup.UserSignUpRequest;
import site.yesaido.user_server.domain.user.entity.Role;
import site.yesaido.user_server.domain.user.entity.User;
import site.yesaido.user_server.domain.user.entity.UserStatus;
import site.yesaido.user_server.domain.user.exception.AlreadyWithdrawnException;
import site.yesaido.user_server.domain.user.exception.EmailDuplicationException;
import site.yesaido.user_server.domain.user.exception.NicknameDuplicationException;
import site.yesaido.user_server.domain.user.exception.UserNotFoundException;
import site.yesaido.user_server.domain.user.repository.UserRepository;
import site.yesaido.user_server.domain.user.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("회원가입 기능 테스트")
    class SingUpTest{
        @Test
        @DisplayName("성공 : 올바른 회원가입 요청 시 비밀번호가 암호화되어 저장된다")
        void success_signUp(){
            UserSignUpRequest requestDto = UserSignUpRequest.builder()
                    .email("rnrn428@naver.com")
                    .password("password123!")
                    .nickName("duplicate")
                    .role(Role.USER)
                    .build();

            User savedUser = User.builder()
                            .email("rnrn428@naver.com")
                                    .password("$2a$10$encodedPassword")
                                            .nickName("duplicate")
                                                    .status(UserStatus.ACTIVE)
                                                            .role(Role.USER)
                                                                    .build();

            given(userRepository.existsByEmail(requestDto.getEmail())).willReturn(false);
            given(userRepository.existsByNickName(requestDto.getNickName())).willReturn(false);
            given(passwordEncoder.encode(requestDto.getPassword())).willReturn("$2a$10$encodedPassword");
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            UserSignResponse responseDto = userService.signUp(requestDto);

            assertThat(responseDto).isNotNull();
            assertThat(responseDto.getEmail()).isEqualTo("rnrn428@naver.com");
            assertThat(responseDto.getNickName()).isEqualTo("duplicate");
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("실패 : 이미 존재하는 이메일로 가입 시 예외가 발생")
        void signUp_DuplicateEmail(){
            UserSignUpRequest requestDto = UserSignUpRequest.builder()
                    .email("rnrn428@naver.com")
                    .password("password123!")
                    .nickName("duplicate_fail")
                    .role(Role.USER)
                    .build();

            given(userRepository.existsByEmail(requestDto.getEmail())).willReturn(true);

            assertThrows(EmailDuplicationException.class, () -> userService.signUp(requestDto));

            verify(userRepository, never()).save(any(User.class));

        }

        @Test
        @DisplayName("실패 : 이미 존재하는 닉네임으로 가입 시 예외 발생")
        void signUp_DuplicateNickname(){
            UserSignUpRequest requestDto = UserSignUpRequest.builder()
                    .email("rnrn428@naver.com")
                    .password("password123!")
                    .nickName("duplicate_fail")
                    .role(Role.USER)
                    .build();

            given(userRepository.existsByNickName(requestDto.getNickName())).willReturn(true);

            assertThrows(NicknameDuplicationException.class, () -> userService.signUp(requestDto));

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("유저 조회 테스트")
    class getUserByIdTest{
        @Test
        @DisplayName("성공 : ID로 존재하는 유저 조회")
        void success_getUserById(){
            User user = User.builder()
                    .id(1L)
                    .email("rnrn428@naver.com")
                    .status(UserStatus.ACTIVE)
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            User foundUser = userService.getUserById(1L);

            assertThat(foundUser).isNotNull();
            assertThat(foundUser.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("실패 : 존재하지 않는 유저 조회 시 예외 발생")
        void getUserById_UserNotFound(){
            given(userRepository.findById(99L)).willReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, ()-> userService.getUserById(99L));

        }

        @Test
        @DisplayName("실패 : 이미 탈퇴한 유저 조회 시 예외 발생")
        void getUserById_AlreadyWithdrawn(){
            User deletedUser = User.builder()
                    .id(1L)
                    .status(UserStatus.DELETED).build();

            given(userRepository.findById(1L)).willReturn(Optional.of(deletedUser));

            assertThrows(AlreadyWithdrawnException.class, () -> userService.getUserById(1L));
        }
    }

    @Nested
    @DisplayName("프로필 수정 테스트 그룹")
    class updateProfileTest{
        @Test
        @DisplayName("닉네임 변경 성공")
        void updateNickname_success(){
            User updateUser = User.builder()
                    .id(1L)
                    .nickName("oldNick")
                    .build();
            String newNick = "newNick";

            given(userRepository.findById(1L)).willReturn(Optional.of(updateUser));
            given(userRepository.existsByNickName(newNick)).willReturn(false);

            userService.updateProfile(1L, newNick);

            assertThat(updateUser.getNickName()).isEqualTo("newNick");

        }

        @Test
        @DisplayName("변경하려는 닉네임이 이미 존재할 시 예외 발생")
        void updateNickname_failed(){
            User updateUser = User.builder()
                    .id(1L)
                    .nickName("oldNick")
                    .build();
            String newNick = "newNick";

            given(userRepository.findById(1L)).willReturn(Optional.of(updateUser));
            given(userRepository.existsByNickName(newNick)).willReturn(true);

            assertThrows(NicknameDuplicationException.class, () -> userService.updateProfile(1L, newNick));
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 테스트")
    class withDrawnTest{
        @Test
        @DisplayName("성공 : 회원 탈퇴 시 상태가 DELETED로 바뀌고 deletedAt이 기록된다")
        void success_withdraw(){
            User user = User.builder()
                    .id(1L)
                    .email("rnrn428@naver.com")
                    .status(UserStatus.ACTIVE)
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            userService.withdraw(1L);

            assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
            assertThat(user.getDeletedAt()).isNotNull();
        }
    }

    @Test
    @DisplayName("이메일이 존재하면 true 반환")
    void existsEmail_true(){
        given(userRepository.existsByEmail("test@test.com")).willReturn(true);

        boolean result = userService.existsEmail("test@test.com");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이메일이 존재하지 않으면 false 반환")
    void existsEmail_false(){
        given(userRepository.existsByEmail("new@test.com")).willReturn(false);

        boolean result = userService.existsEmail("new@test.com");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("닉네임이 존재하면 true 반환")
    void existNickname_true(){
        given(userRepository.existsByNickName("닉네임")).willReturn(true);

        boolean result = userService.existNickname("닉네임");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("닉네임이 존재하지 않으면 false 반환")
    void existNickname_false(){
        given(userRepository.existsByNickName("새닉네임")).willReturn(false);

        boolean result = userService.existNickname("새닉네임");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("키워드가 비어있으면 빈 리스트를 반환하고 저장소를 호출하지 않는다")
    void searchUsers_blankKeyword(){
        List<UserSearchResponse> result = userService.searchUsers("   ");

        assertThat(result).isEmpty();
        verify(userRepository, never()).searchActiveUsers(any(), any());
    }

    @Test
    @DisplayName("키워드로 활성 사용자를 검색해서 반환한다")
    void searchUsers_success(){
        User user = User.builder()
                .id(1L)
                .nickName("닉네임")
                .status(UserStatus.ACTIVE)
                .build();

        given(userRepository.searchActiveUsers("닉네임", UserStatus.DELETED))
                .willReturn(List.of(user));

        List<UserSearchResponse> result = userService.searchUsers("닉네임");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nickname()).isEqualTo("닉네임");
    }

    @Test
    @DisplayName("ID 목록으로 사용자 요약 정보를 조회한다")
    void getUsers_success(){
        User user1 = User.builder().id(1L).nickName("닉네임1").build();
        User user2 = User.builder().id(2L).nickName("닉네임2").build();

        given(userRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(user1, user2));

        List<UserSummaryResponse> result = userService.getUsers(List.of(1L, 2L));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).nickname()).isEqualTo("닉네임1");
    }
}




















