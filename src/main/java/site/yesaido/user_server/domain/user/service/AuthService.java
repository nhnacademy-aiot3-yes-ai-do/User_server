package site.yesaido.user_server.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yesaido.user_server.domain.user.dto.login.LoginRequest;
import site.yesaido.user_server.domain.user.dto.oauth.GoogleLoginRequest;
import site.yesaido.user_server.domain.user.dto.token.TokenResponse;
import site.yesaido.user_server.domain.user.entity.User;
import site.yesaido.user_server.domain.user.entity.en.UserStatus;
import site.yesaido.user_server.domain.user.exception.*;
import site.yesaido.user_server.domain.user.repository.UserRepository;
import site.yesaido.user_server.global.jwt.JwtTokenProvider;
import site.yesaido.user_server.global.oauth.GoogleTokenVerifier;

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
    private final GoogleTokenVerifier googleTokenVerifier;

    @Transactional
    public TokenResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다."));

        log.info("[로그인 시도] 유저 DB상태: {}", user.getStatus());


        if(!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new InvalidPasswordException();
        }

        return createTokenResponse(user);
    }

    @Transactional
    public TokenResponse loginWithGoogle(GoogleLoginRequest request){
        String verifiedEmail = googleTokenVerifier.verifyAndGetEmail(request.idToken());
        if (verifiedEmail == null || verifiedEmail.isBlank()) {
            throw new InvalidTokenException("유효하지 않은 Google ID Token입니다.");
        }

        String baseNick = (request.nickName() != null && !request.nickName().isBlank())
                ? request.nickName().trim()
                : "google_user";

        if (baseNick.length() > 35) {
            baseNick = baseNick.substring(0, 35);
        }
        String uniqueNickname = baseNick + "_" + java.util.UUID.randomUUID().toString().substring(0, 8);

        User user = userRepository.findByEmail(verifiedEmail)
                .orElseGet(() -> {
                    log.info("[구글 신규 소셜 회원가입] 이메일: {}, 닉네임: {}", verifiedEmail, uniqueNickname);
                    User newUser = new User(verifiedEmail, uniqueNickname);
                    return userRepository.save(newUser);
                });

        return createTokenResponse(user);
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

        String newAccessToken = jwtTokenProvider.createAccessToken(userId, user.getRole());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, user.getRole());

        stringRedisTemplate.opsForValue().set("RT:" + user.getId(), newRefreshToken, 14, TimeUnit.DAYS);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .role(user.getRole())
                .build();
    }

    @Transactional
    public void logout(Long userId){
        stringRedisTemplate.delete("RT:" + userId);
    }

    @Transactional
    public void releaseDormant(String email){
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 사용자입니다."));

        user.activate();
    }

    private TokenResponse createTokenResponse(User user){
        if(UserStatus.DELETED.equals(user.getStatus())){
            throw new AlreadyWithdrawnException("탈퇴한 사용자입니다.");
        }

        if(UserStatus.DORMANT.equals(user.getStatus())){
            throw new DormantUserException("휴면 계정입니다. 이메일 인증을 진행해 주세요.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getRole());

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
                .role(user.getRole())
                .build();
    }

}
