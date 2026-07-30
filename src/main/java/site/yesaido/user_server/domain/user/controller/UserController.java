package site.yesaido.user_server.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.yesaido.user_server.domain.user.dto.UserSummaryResponse;
import site.yesaido.user_server.domain.user.dto.search.UserSearchResponse;
import site.yesaido.user_server.domain.user.dto.signup.UserSignResponse;
import site.yesaido.user_server.domain.user.dto.signup.UserSignUpRequest;
import site.yesaido.user_server.domain.user.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam("email") String email){
        boolean isDuplicated = userService.existsEmail(email);

        return ResponseEntity.ok(isDuplicated);
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam("nickname") String nickName){
        boolean isDuplicated = userService.existNickname(nickName);
        return ResponseEntity.ok(isDuplicated);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserSignResponse> signUp(@Valid @RequestBody UserSignUpRequest signUpRequestDto){
        UserSignResponse responseDto = userService.signUp(signUpRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // 재배 멤버 초대용: 닉네임 부분일치 또는 이메일 완전일치로 사용자 검색
    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponse>> search(@RequestParam("keyword") String keyword){
        List<UserSearchResponse> response = userService.searchUsers(keyword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/batch")
    public ResponseEntity<List<UserSummaryResponse>> getUsers(@RequestParam("ids") List<Long> ids){
        List<UserSummaryResponse> response = userService.getUsers(ids);
        return ResponseEntity.ok(response);
    }

}
