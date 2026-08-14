package site.yesaido.user_server.domain.user.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import site.yesaido.user_server.domain.user.entity.en.UserStatus;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {
    @Test
    @DisplayName("성공 : 휴먼 계정 전환 및 활성화 메서드 테스트")
    void user_status_change_test(){
        User user = new User("nhn123@naver.com", "nhn123!", "newNick");

        user.changeToDormant();
        assertThat(user.getStatus()).isEqualTo(UserStatus.DORMANT);

        user.activate();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getLastLoginAt()).isNotNull();

    }
}
