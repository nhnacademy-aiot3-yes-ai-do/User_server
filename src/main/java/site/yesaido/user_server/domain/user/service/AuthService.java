package site.yesaido.user_server.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yesaido.user_server.domain.user.dto.token.TokenResponse;
import site.yesaido.user_server.domain.user.dto.login.LoginRequest;
import site.yesaido.user_server.domain.user.entity.User;
import site.yesaido.user_server.domain.user.entity.UserStatus;
import site.yesaido.user_server.domain.user.exception.*;
import site.yesaido.user_server.domain.user.repository.UserRepository;
import site.yesaido.user_server.global.jwt.JwtTokenProvider;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate stringRedisTemplate;

    @Transactional
    public TokenResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다."));

        if(UserStatus.DELETED.equals(user.getStatus())){
            throw new AlreadyWithdrawnException("탈퇴한 사용자입니다.");
        }

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        stringRedisTemplate.opsForValue().set(
                "RT:" + user.getId(),
                refreshToken,
                14,
                TimeUnit.DAYS
        );

        user.updateLastLoginAt();

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

    public TokenResponse reissue(String refreshToken){
        if(!jwtTokenProvider.validateToken(refreshToken)){
            throw new InvalidTokenException("유효하지 않거나 만료된 RefreshToken 입니다.");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        String savedRefreshToken = stringRedisTemplate.opsForValue().get("RT:" + userId);

        if(savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)){
            throw new InvalidTokenException("레디스 토큰과 일치하지 않습니다. (로그아웃 또는 해킹 위험이 있습니다.)");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

        String newAccessToken = jwtTokenProvider.createAccessToken(userId, user.getEmail(), user.getRole());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

        stringRedisTemplate.opsForValue().set("RT:" + user.getId(), newRefreshToken, 14, TimeUnit.DAYS);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Transactional
    public void logout(Long userId){
        stringRedisTemplate.delete("RT:" + userId);
    }




}
