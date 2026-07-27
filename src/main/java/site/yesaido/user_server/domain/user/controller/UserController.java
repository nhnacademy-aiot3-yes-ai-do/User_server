package site.yesaido.user_server.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.yesaido.user_server.domain.user.dto.signup.UserSignResponse;
import site.yesaido.user_server.domain.user.dto.signup.UserSignUpRequest;
import site.yesaido.user_server.domain.user.service.UserService;

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


}
