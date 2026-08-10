package site.yesaido.user_server.domain.inquiry.service.impl;

import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yesaido.user_server.domain.inquiry.dto.request.InquiryCreateRequest;
import site.yesaido.user_server.domain.inquiry.dto.request.InquiryMessageRequest;
import site.yesaido.user_server.domain.inquiry.dto.response.InquiryCategoryResponse;
import site.yesaido.user_server.domain.inquiry.dto.response.InquiryDetailResponse;
import site.yesaido.user_server.domain.inquiry.dto.response.InquiryMessageResponse;
import site.yesaido.user_server.domain.inquiry.dto.response.InquirySummaryResponse;
import site.yesaido.user_server.domain.inquiry.entity.Inquiry;
import site.yesaido.user_server.domain.inquiry.entity.InquiryAnswer;
import site.yesaido.user_server.domain.inquiry.entity.InquiryCategory;
import site.yesaido.user_server.domain.inquiry.entity.InquiryStatus;
import site.yesaido.user_server.domain.inquiry.exception.InquiryAccessDeniedException;
import site.yesaido.user_server.domain.inquiry.exception.InquiryAnswerNotFoundException;
import site.yesaido.user_server.domain.inquiry.exception.InquiryCategoryNotFoundException;
import site.yesaido.user_server.domain.inquiry.exception.InquiryNotFoundException;
import site.yesaido.user_server.domain.inquiry.repository.InquiryAnswerRepository;
import site.yesaido.user_server.domain.inquiry.repository.InquiryCategoryRepository;
import site.yesaido.user_server.domain.inquiry.repository.InquiryRepository;
import site.yesaido.user_server.domain.inquiry.service.InquiryService;
import site.yesaido.user_server.domain.user.entity.Role;
import site.yesaido.user_server.domain.user.entity.User;
import site.yesaido.user_server.domain.user.exception.UserNotFoundException;
import site.yesaido.user_server.domain.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryServiceImpl implements InquiryService {
    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final InquiryCategoryRepository inquiryCategoryRepository;
    private final UserRepository userRepository;

    @Override
    public List<InquiryCategoryResponse> getCategories() {
        return inquiryCategoryRepository.findAll().stream()
                .map(InquiryCategoryResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public InquiryDetailResponse createInquiry(Long userId, InquiryCreateRequest inquiryCreateRequest) {
        InquiryCategory category = inquiryCategoryRepository.findById(inquiryCreateRequest.getCategoryId())
                .orElseThrow(InquiryCategoryNotFoundException::new);

        Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                .userId(userId)
                .category(category)
                .title(inquiryCreateRequest.getTitle())
                .build());

        InquiryAnswer rootMessage = InquiryAnswer.builder()
                .inquiry(inquiry)
                .content(inquiryCreateRequest.getContent())
                .build();

        return InquiryDetailResponse.of(inquiry, List.of(rootMessage));
    }

    public Page<InquirySummaryResponse> getMyInquiries(Long userId, Pageable pageable) {
        return inquiryRepository.findAllByUserId(userId, pageable)
                .map(InquirySummaryResponse::from);
    }

    public InquiryDetailResponse getMyInquiryDetail(Long userId, Long inquiryId) {
        Inquiry inquiry = getInquiryOrThrow(inquiryId);
        requireOwner(inquiry, userId);

        List<InquiryAnswer> messages = inquiryAnswerRepository.findAllByInquiryIdOrderByCreatedAtAsc(inquiryId);
        return InquiryDetailResponse.of(inquiry, messages);
    }

    @Override
    @Transactional
    public InquiryDetailResponse addFollowUp(Long userId, Long inquiryId, InquiryMessageRequest request) {
        Inquiry inquiry = getInquiryOrThrow(inquiryId);
        requireOwner(inquiry, userId);

        InquiryAnswer latest = inquiryAnswerRepository.findTopByInquiryIdOrderByCreatedAtDesc(inquiryId)
                .orElseThrow(InquiryAnswerNotFoundException::new);

        inquiryAnswerRepository.save(InquiryAnswer.builder()
                .inquiry(inquiry)
                .content(request.getContent())
                .pre(latest)
                .build());

        inquiry.markPending();

        List<InquiryAnswer> messages = inquiryAnswerRepository.findAllByInquiryIdOrderByCreatedAtAsc(inquiryId);
        return InquiryDetailResponse.of(inquiry, messages);
    }

    // 관리자 용
    @Override
    public Page<InquirySummaryResponse> getAllInquiries(Long adminUserId, InquiryStatus statusFilter, Pageable pageable) {
        requireAdmin(adminUserId);

        Page<Inquiry> page = statusFilter != null
                ? inquiryRepository.findAllByStatus(statusFilter, pageable)
                :inquiryRepository.findAll(pageable);

        return page.map(InquirySummaryResponse::from);
    }

    @Override
    public InquiryDetailResponse getInquiryDetailForAdmin(Long adminUserId, Long inquiryId) {
        requireAdmin(adminUserId);

        Inquiry inquiry = getInquiryOrThrow(inquiryId);
        List<InquiryAnswer> messages = inquiryAnswerRepository.findAllByInquiryIdOrderByCreatedAtAsc(inquiryId);
        return InquiryDetailResponse.of(inquiry, messages);
    }

    @Override
    @Transactional
    public InquiryDetailResponse answerMessage(Long adminUserId, Long answerId, InquiryMessageRequest request) {
        requireAdmin(adminUserId);

        InquiryAnswer message = inquiryAnswerRepository.findById(answerId)
                .orElseThrow(InquiryAnswerNotFoundException::new);

        message.answer(request.getContent());

        Inquiry inquiry = message.getInquiry();
        inquiry.markResolved();

        List<InquiryAnswer> messages = inquiryAnswerRepository.findAllByInquiryIdOrderByCreatedAtAsc(inquiry.getId());
        return InquiryDetailResponse.of(inquiry, messages);
    }

    // Helper Method
    private void requireAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        if (user.getRole() != Role.ADMIN) {
            throw new InquiryAccessDeniedException("관리자만 접근할 수 있습니다.");
        }
    }

    private void requireOwner(Inquiry inquiry, Long userId) {
        if (!inquiry.getUserId().equals(userId)) {
            throw new InquiryAccessDeniedException();
        }
    }

    private Inquiry getInquiryOrThrow(Long inquiryId) {
        return inquiryRepository.findById(inquiryId)
                .orElseThrow(InquiryNotFoundException::new);
    }
}
