package site.yesaido.user_server.global.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleTokenVerifier {
    private final RestClient restClient = RestClient.create();

    public String verifyAndGetEmail(String idToken){
        try{
            Map<String, Object> response = restClient.get()
                    .uri("https://oauth2.googleapis.com/tokeninfo?id_token={token}", idToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if(response != null && "true".equals(String.valueOf(response.get("email_verified")))){
                return (String) response.get("email");
            }
        }catch (Exception e){
            log.warn("Google ID Token 검증 실패 : {}", e.getMessage());
        }
        return null;
    }
}
