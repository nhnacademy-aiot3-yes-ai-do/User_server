package site.yesaido.user_server.global.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import site.yesaido.user_server.global.exception.InvalidPageRequestException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageRequestValidatorTest {

    @Test
    @DisplayName("유효한 page와 size 전달 시 정상적으로 Pageable을 생성한다")
    void of_valid_success() {
        Pageable pageable = PageRequestValidator.of(0, 20);

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("page가 null이거나 음수이면 InvalidPageRequestException 발생")
    void of_invalidPage_throwsException() {
        assertThatThrownBy(() -> PageRequestValidator.of(null, 20))
                .isInstanceOf(InvalidPageRequestException.class)
                .hasMessageContaining("page는 0 이상이어야 합니다.");

        assertThatThrownBy(() -> PageRequestValidator.of(-1, 20))
                .isInstanceOf(InvalidPageRequestException.class)
                .hasMessageContaining("page는 0 이상이어야 합니다.");
    }

    @Test
    @DisplayName("size가 null이거나 1 미만이거나 100 초과이면 InvalidPageRequestException 발생")
    void of_invalidSize_throwsException() {
        assertThatThrownBy(() -> PageRequestValidator.of(0, null))
                .isInstanceOf(InvalidPageRequestException.class)
                .hasMessageContaining("size는 1 이상 100 이하이어야 합니다.");

        assertThatThrownBy(() -> PageRequestValidator.of(0, 0))
                .isInstanceOf(InvalidPageRequestException.class)
                .hasMessageContaining("size는 1 이상 100 이하이어야 합니다.");

        assertThatThrownBy(() -> PageRequestValidator.of(0, 101))
                .isInstanceOf(InvalidPageRequestException.class)
                .hasMessageContaining("size는 1 이상 100 이하이어야 합니다.");
    }
}
