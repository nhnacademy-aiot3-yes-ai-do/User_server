package site.yesaido.user_server.domain.user.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import site.yesaido.user_server.domain.user.controller.UserController;
import site.yesaido.user_server.domain.user.dto.UserSummaryResponse;
import site.yesaido.user_server.domain.user.dto.search.UserSearchResponse;
import site.yesaido.user_server.domain.user.dto.signup.UserSignResponse;
import site.yesaido.user_server.domain.user.dto.signup.UserSignUpRequest;
import site.yesaido.user_server.domain.user.entity.Role;
import site.yesaido.user_server.domain.user.entity.UserStatus;
import site.yesaido.user_server.domain.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("이메일 중복 확인 - 중복이면 true 반환")
    void checkEmail_duplicated() {
        given(userService.existsEmail("test@test.com")).willReturn(true);

        ResponseEntity<Boolean> response = userController.checkEmail("test@test.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
    }

    @Test
    @DisplayName("이메일 중복 확인 - 중복 아니면 false 반환")
    void checkEmail_notDuplicated() {
        given(userService.existsEmail("new@test.com")).willReturn(false);

        ResponseEntity<Boolean> response = userController.checkEmail("new@test.com");

        assertThat(response.getBody()).isFalse();
    }

    @Test
    @DisplayName("닉네임 중복 확인 - 중복이면 true 반환")
    void checkNickname_duplicated() {
        given(userService.existNickname("닉네임")).willReturn(true);

        ResponseEntity<Boolean> response = userController.checkNickname("닉네임");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
    }

    @Test
    @DisplayName("닉네임 중복 확인 - 중복 아니면 false 반환")
    void checkNickname_notDuplicated() {
        given(userService.existNickname("새닉네임")).willReturn(false);

        ResponseEntity<Boolean> response = userController.checkNickname("새닉네임");

        assertThat(response.getBody()).isFalse();
    }

    @Test
    @DisplayName("회원가입 성공 시 201과 가입 정보를 반환한다")
    void signUp_success() {
        UserSignUpRequest request = UserSignUpRequest.builder()
                .email("test@test.com")
                .password("password1!")
                .nickName("닉네임")
                .role(Role.USER)
                .build();

        UserSignResponse expected = UserSignResponse.builder()
                .id(1L)
                .email("test@test.com")
                .nickName("닉네임")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        given(userService.signUp(request)).willReturn(expected);

        ResponseEntity<UserSignResponse> response = userController.signUp(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("test@test.com");
        assertThat(response.getBody().getNickName()).isEqualTo("닉네임");
    }

    @Test
    @DisplayName("사용자 검색 결과를 그대로 반환한다")
    void search_success() {
        List<UserSearchResponse> expected = List.of(new UserSearchResponse(1L, "닉네임"));
        given(userService.searchUsers("닉")).willReturn(expected);

        ResponseEntity<List<UserSearchResponse>> response = userController.search("닉");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).nickname()).isEqualTo("닉네임");
    }

    @Test
    @DisplayName("ID 목록으로 사용자 요약 정보를 배치 조회한다")
    void getUsers_success() {
        List<UserSummaryResponse> expected = List.of(
                new UserSummaryResponse(1L, "닉네임1"),
                new UserSummaryResponse(2L, "닉네임2")
        );
        given(userService.getUsers(List.of(1L, 2L))).willReturn(expected);

        ResponseEntity<List<UserSummaryResponse>> response = userController.getUsers(List.of(1L, 2L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }
}