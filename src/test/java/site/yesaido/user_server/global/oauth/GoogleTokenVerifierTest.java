package site.yesaido.user_server.global.oauth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GoogleTokenVerifierTest {

    private GoogleTokenVerifier googleTokenVerifier;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        googleTokenVerifier = new GoogleTokenVerifier();
        ReflectionTestUtils.setField(googleTokenVerifier, "restClient", builder.build());
    }

    @Test
    @DisplayName("성공 - 이메일 인증(email_verified=true) 완료된 구글 토큰인 경우 이메일을 반환한다")
    void verifyAndGetEmail_verified_returnsEmail() {
        mockServer.expect(requestTo("https://oauth2.googleapis.com/tokeninfo?id_token=valid-token"))
                .andRespond(withSuccess("{\"email_verified\": \"true\", \"email\": \"user@gmail.com\"}", MediaType.APPLICATION_JSON));

        String email = googleTokenVerifier.verifyAndGetEmail("valid-token");

        assertThat(email).isEqualTo("user@gmail.com");
        mockServer.verify();
    }

    @Test
    @DisplayName("실패 - 이메일 미인증(email_verified=false) 토큰인 경우 null을 반환한다")
    void verifyAndGetEmail_notVerified_returnsNull() {
        mockServer.expect(requestTo("https://oauth2.googleapis.com/tokeninfo?id_token=unverified-token"))
                .andRespond(withSuccess("{\"email_verified\": \"false\", \"email\": \"user@gmail.com\"}", MediaType.APPLICATION_JSON));

        String email = googleTokenVerifier.verifyAndGetEmail("unverified-token");

        assertThat(email).isNull();
        mockServer.verify();
    }

    @Test
    @DisplayName("실패 - 응답 바디에 email_verified가 없는 경우 null을 반환한다")
    void verifyAndGetEmail_emptyResponse_returnsNull() {
        mockServer.expect(requestTo("https://oauth2.googleapis.com/tokeninfo?id_token=empty-token"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        String email = googleTokenVerifier.verifyAndGetEmail("empty-token");

        assertThat(email).isNull();
        mockServer.verify();
    }

    @Test
    @DisplayName("실패 - 구글 서버 에러 발생 시 예외를 로깅하고 null을 반환한다")
    void verifyAndGetEmail_serverError_returnsNull() {
        mockServer.expect(requestTo("https://oauth2.googleapis.com/tokeninfo?id_token=error-token"))
                .andRespond(withServerError());

        String email = googleTokenVerifier.verifyAndGetEmail("error-token");

        assertThat(email).isNull();
        mockServer.verify();
    }
}
