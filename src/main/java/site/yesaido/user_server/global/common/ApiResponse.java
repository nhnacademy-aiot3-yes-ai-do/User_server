package site.yesaido.user_server.global.common;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
        @JsonIgnore
        HttpStatus httpStatus,
        boolean success,
        String message,
        T data
){
    // 1. 200 OK 성공 응답 (데이터 포함)
    public static <T> ApiResponse<T> ok(String message, T data){
        return new ApiResponse<>(HttpStatus.OK, true, message, data);
    }

    // 2. 200 OK 성공 응답 (데이터 없음)
    public static <T> ApiResponse<T> ok(String message){
        return new ApiResponse<>(HttpStatus.OK, true, message, null);
    }

    // 3. 201 CREATED 생성 성공 응답 (회원가입 완료 시)
    public static <T> ApiResponse<T> created(String message, T data){
        return new ApiResponse<>(HttpStatus.CREATED, true, message, data);
    }

    // 4. 실패/에러 응답 (상태코드 + 에러 메시지)
    public static <T> ApiResponse<T> fail(HttpStatus status, String message){
        return new ApiResponse<>(status, false, message, null);
    }


}
