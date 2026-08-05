package site.yesaido.user_server.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.yesaido.user_server.domain.user.dto.UserSummaryResponse;
import site.yesaido.user_server.domain.user.dto.search.UserSearchResponse;
import site.yesaido.user_server.domain.user.dto.signup.UserSignResponse;
import site.yesaido.user_server.domain.user.dto.signup.UserSignUpRequest;
import site.yesaido.user_server.domain.user.service.UserService;
import site.yesaido.user_server.global.common.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    // 1. 이메일 중복 체크
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam("email") String email){
        boolean isDuplicated = userService.existsEmail(email);
        ApiResponse<Boolean> apiResponse = ApiResponse.ok("이메일 중복 체크 결과입니다.", isDuplicated);
        return ResponseEntity.status(apiResponse.httpStatus()).body(apiResponse);
    }

    // 2. 닉네임 중복 체크
    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam("nickname") String nickName){
        boolean isDuplicated = userService.existNickname(nickName);
        ApiResponse<Boolean> apiResponse = ApiResponse.ok("닉네임 중복 체크 결과입니다.", isDuplicated);
        return ResponseEntity.status(apiResponse.httpStatus()).body(apiResponse);
    }

    // 3. 회원가입 (201 Created)
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserSignResponse>> signUp(@Valid @RequestBody UserSignUpRequest signUpRequestDto){
        UserSignResponse responseDto = userService.signUp(signUpRequestDto);
        ApiResponse<UserSignResponse> apiResponse = ApiResponse.created("회원가입이 성공적으로 완료되었습니다.", responseDto);
        return ResponseEntity.status(apiResponse.httpStatus()).body(apiResponse);
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
