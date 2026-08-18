package site.yesaido.user_server.domain.user.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import site.yesaido.user_server.domain.user.entity.User;
import site.yesaido.user_server.domain.user.entity.en.UserStatus;
import site.yesaido.user_server.domain.user.repository.UserRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class DormantUserSchedulerTest {
    @Mock
    private UserRepository userRepository;

    @Spy
    private Clock clock = Clock.systemDefaultZone();

    @InjectMocks
    private DormantUserScheduler userScheduler;

    @Test
    @DisplayName("성공 : 1년 이상 미접속 회원들을 조회하여 휴면 상태로 자동 전환한다")
    void processDormantUsers_success(){
        User activeUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .status(UserStatus.ACTIVE)
                .build();

        given(userRepository.findDormantCandidates(eq(UserStatus.ACTIVE), any(LocalDateTime.class)))
                .willReturn(List.of(activeUser));

        userScheduler.processDormantUsers();

        assertThat(activeUser.getStatus()).isEqualTo(UserStatus.DORMANT);
    }
    @Test
    @DisplayName("성공 : 휴면 대상 회원이 없으면 변경 작업 없이 무사히 종료된다")
    void processDormantUsers_emptyList(){
        given(userRepository.findDormantCandidates(eq(UserStatus.ACTIVE), any(LocalDateTime.class)))
                .willReturn(Collections.emptyList());

        userScheduler.processDormantUsers();

        assertDoesNotThrow( () -> userScheduler.processDormantUsers());
    }
}
