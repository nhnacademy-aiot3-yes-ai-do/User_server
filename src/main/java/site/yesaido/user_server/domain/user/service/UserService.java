package site.yesaido.user_server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import site.yesaido.user_server.domain.user.dto.UserSummaryResponse;
import site.yesaido.user_server.domain.user.dto.profile.ProfileUpdateRequest;
import site.yesaido.user_server.domain.user.dto.profile.UserProfileResponse;
import site.yesaido.user_server.domain.user.dto.search.UserSearchResponse;
import site.yesaido.user_server.domain.user.dto.signup.UserSignResponse;
import site.yesaido.user_server.domain.user.dto.signup.UserSignUpRequest;
import site.yesaido.user_server.domain.user.entity.User;
import site.yesaido.user_server.domain.user.entity.UserStatus;
import site.yesaido.user_server.domain.user.exception.*;
import site.yesaido.user_server.domain.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserSignResponse signUp(UserSignUpRequest signUpRequestDto){
        if(userRepository.existsByEmail(signUpRequestDto.getEmail())){
            throw new EmailDuplicationException("이메일이 중복됩니다.");
        }

        if(userRepository.existsByNickName(signUpRequestDto.getNickName())){
            throw new NicknameDuplicationException("닉네임이 중복됩니다.");
        }

        String encodedPassword = passwordEncoder.encode(signUpRequestDto.getPassword());

        User user = User.builder()
                .email(signUpRequestDto.getEmail())
                .password(encodedPassword)
                .nickName(signUpRequestDto.getNickName())
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        return UserSignResponse.from(savedUser);

    }

    public User getUserById(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("아이디를 찾을 수 없습니다."));

        if(UserStatus.DELETED.equals(user.getStatus())){
            throw new AlreadyWithdrawnException("이미 탈퇴한 사용자입니다.");
        }

        return user;
    }

    public UserProfileResponse getMyProfile(Long userId){
        User user = getUserById(userId);
        return UserProfileResponse.from(user);
    }

    public boolean verifyPassword(Long userId, String rawPassword){
        if(userId == null){
            return false;
        }
        User user = getUserById(userId);
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, ProfileUpdateRequest request){
        User user = getUserById(userId);

        if(StringUtils.hasText(request.nickname()) && !user.getNickName().equals(request.nickname())){
            if(userRepository.existsByNickName(request.nickname())){
                throw new NicknameDuplicationException("이미 사용 중인 닉네임입니다.");
            }
            user.updateNickname(request.nickname());
        }

        if(StringUtils.hasText(request.newPassword())){
            if (!StringUtils.hasText(request.currentPassword())) {
                throw new InvalidPasswordException("현재 비밀번호를 입력해 주세요.");
            }
            if(!passwordEncoder.matches(request.currentPassword(), user.getPassword())){
                throw new InvalidPasswordException("현재 비밀번호가 일치하지 않습니다.");
            }
            user.updatePassword(passwordEncoder.encode(request.newPassword()));
        }
        return UserProfileResponse.from(user);
    }


    @Transactional
    public void withdraw(Long userId) {
        User user = getUserById(userId);
        user.withdraw();
    }

    public boolean existsEmail(String email){
        return userRepository.existsByEmail(email);
    }

    public boolean existNickname(String nickName){
        return userRepository.existsByNickName(nickName);
    }

    // 재배 멤버 초대용: 닉네임 부분일치 또는 이메일 완전일치로 활성 사용자 검색
    public List<UserSearchResponse> searchUsers(String keyword){
        if(!StringUtils.hasText(keyword)){
            return Collections.emptyList();
        }

        return userRepository.searchActiveUsers(keyword.trim(), UserStatus.DELETED).stream()
                .map(UserSearchResponse::from)
                .toList();
    }

    public List<UserSummaryResponse> getUsers(List<Long> userIds){
        return userRepository.findAllById(userIds).stream()
                .map(UserSummaryResponse::from)
                .toList();
    }

}
