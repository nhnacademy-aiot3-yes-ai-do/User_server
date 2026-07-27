package site.yesaido.user_server.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yesaido.user_server.domain.user.dto.signup.UserSignResponse;
import site.yesaido.user_server.domain.user.dto.signup.UserSignUpRequest;
import site.yesaido.user_server.domain.user.entity.User;
import site.yesaido.user_server.domain.user.entity.UserStatus;
import site.yesaido.user_server.domain.user.exception.AlreadyWithdrawnException;
import site.yesaido.user_server.domain.user.exception.EmailDuplicationException;
import site.yesaido.user_server.domain.user.exception.NicknameDuplicationException;
import site.yesaido.user_server.domain.user.exception.UserNotFoundException;
import site.yesaido.user_server.domain.user.repository.UserRepository;

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

    @Transactional
    public void updateProfile(Long userId, String newNickname){
        User user = getUserById(userId);

        if(!user.getNickName().equals(newNickname) && userRepository.existsByNickName(newNickname)){
            throw new NicknameDuplicationException("이미 사용 중인 닉네임입니다.");
        }

        user.updateNickname(newNickname);
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




}
