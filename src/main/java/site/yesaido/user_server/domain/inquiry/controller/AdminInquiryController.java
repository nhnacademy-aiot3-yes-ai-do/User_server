package site.yesaido.user_server.domain.inquiry.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import site.yesaido.user_server.domain.inquiry.dto.request.InquiryMessageRequest;
import site.yesaido.user_server.domain.inquiry.dto.response.InquiryDetailResponse;
import site.yesaido.user_server.domain.inquiry.dto.response.InquiryMessageResponse;
import site.yesaido.user_server.domain.inquiry.dto.response.InquirySummaryResponse;
import site.yesaido.user_server.domain.inquiry.entity.InquiryStatus;
import site.yesaido.user_server.domain.inquiry.service.InquiryService;
import site.yesaido.user_server.global.common.ApiResponse;
import site.yesaido.user_server.global.common.PageRequestValidator;

@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {
    private final InquiryService inquiryService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<InquirySummaryResponse>>> getAllInquirySummary(
            @RequestHeader("X-User-Id") Long adminUserId,
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Pageable pageable = PageRequestValidator.of(page, size);
        Page<InquirySummaryResponse> responses = inquiryService.getAllInquiries(adminUserId, status, pageable);
        ApiResponse<Page<InquirySummaryResponse>> apiResponse = ApiResponse.ok("전체 문의 목록입니다.", responses);
        return ResponseEntity.status(apiResponse.httpStatus()).body(apiResponse);
    }

    @GetMapping("/{inquiry-id}")
    public ResponseEntity<ApiResponse<InquiryDetailResponse>> getInquiryDetail(@RequestHeader("X-User-Id") Long adminUserId,
                                                                               @PathVariable("inquiry-id") Long inquiryId) {
        InquiryDetailResponse response = inquiryService.getInquiryDetailForAdmin(adminUserId, inquiryId);
        ApiResponse<InquiryDetailResponse> apiResponse = ApiResponse.ok("문의 상세입니다.", response);
        return ResponseEntity.status(apiResponse.httpStatus()).body(apiResponse);
    }

    @PutMapping("/messages/{answer-id}")
    public ResponseEntity<ApiResponse<InquiryDetailResponse>> answerMessage(@RequestHeader("X-User-Id") Long adminUserId,
                                                                            @PathVariable("answer-id") Long answerId,
                                                                            @Valid @RequestBody InquiryMessageRequest inquiryMessageRequest) {
        InquiryDetailResponse response = inquiryService.answerMessage(adminUserId, answerId, inquiryMessageRequest);
        ApiResponse<InquiryDetailResponse> apiResponse = ApiResponse.ok("답변이 등록되었습니다.", response);
        return ResponseEntity.status(apiResponse.httpStatus()).body(apiResponse);
    }
}
