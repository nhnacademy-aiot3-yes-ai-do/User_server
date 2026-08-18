package site.yesaido.user_server;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserServerApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("스프링 컨텍스트가 정상적으로 로드된다")
    void contextLoads() {
        assertThat(context).isNotNull();
    }

}
