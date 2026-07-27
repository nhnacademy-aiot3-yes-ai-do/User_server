package site.yesaido.auth_server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yesaido.auth_server.domain.user.dto.TokenResponse;
import site.yesaido.auth_server.domain.user.dto.login.LoginRequest;
import site.yesaido.auth_server.domain.user.entity.User;
import site.yesaido.auth_server.domain.user.entity.UserStatus;
import site.yesaido.auth_server.domain.user.exception.AlreadyWithdrawnException;
import site.yesaido.auth_server.domain.user.exception.UserNotFoundException;
import site.yesaido.auth_server.domain.user.repository.UserRepository;
import site.yesaido.auth_server.global.jwt.JwtTokenProvider;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public TokenResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다."));

        if(UserStatus.DELETED.equals(user.getStatus())){
            throw new AlreadyWithdrawnException("탈퇴한 사용자입니다.");
        }

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        user.updateLastLoginAt();

        return TokenResponse.builder()
                .type("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expireInl(1800L)
                .build();
    }
}
