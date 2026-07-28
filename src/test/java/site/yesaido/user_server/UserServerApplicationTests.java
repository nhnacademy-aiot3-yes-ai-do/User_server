package site.yesaido.user_server;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserServerApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("성공 : 스프링 부트 애플리케이션 컨텍스트가 정상 로드된다")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

}
