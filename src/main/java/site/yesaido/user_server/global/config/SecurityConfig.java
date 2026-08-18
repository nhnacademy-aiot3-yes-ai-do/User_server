package site.yesaido.user_server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import site.yesaido.user_server.global.exception.SecurityFilterChainConfigurationException;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * API Gateway에서 JWT 인증/인가를 선행 처리하므로,
     * 내부 마이크로서비스(Auth_server)는 무상태(Stateless) REST API로 최적화합니다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        try{
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .formLogin(AbstractHttpConfigurer::disable)
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()
                    )
                    .build();
        }catch (Exception e){
            throw new SecurityFilterChainConfigurationException("시큐리티 필터체인 구성에 실패했습니다.", e);
            }
        }
        
}
