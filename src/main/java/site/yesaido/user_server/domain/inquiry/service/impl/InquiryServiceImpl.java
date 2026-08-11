package site.yesaido.user_server.domain.inquiry.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.yesaido.user_server.domain.inquiry.client.CultivationClient;
import site.yesaido.user_server.domain.inquiry.dto.request.InquiryCreateRequest;
import site.yesaido.user_server.domain.inquiry.dto.request.InquiryMessageRequest;
import site.yesaido.user_server.domain.inquiry.dto.response.CultivationSummaryResponse;
import site.yesaido.user_server.domain.inquiry.dto.response.InquiryCategoryResponse;
import site.yesaido.user_server.domain.inquiry.dto.response.InquiryDetailResponse;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryServiceImpl implements InquiryService {
    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final InquiryCategoryRepository inquiryCategoryRepository;
    private final UserRepository userRepository;
    private final CultivationClient cultivationClient;

    @Override
    public List<InquiryCategoryResponse> getCategories() {
        return inquiryCategoryRepository.findAll().stream()
                .map(InquiryCategoryResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public InquiryDetailResponse createInquiry(Long userId, InquiryCreateRequest request) {
        InquiryCategory category = inquiryCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(InquiryCategoryNotFoundException::new);

        Inquiry inquiry = inquiryRepository.save(Inquiry.builder()
                .userId(userId)
                .category(category)
                .title(request.getTitle())
                .cultivationId(request.getCultivationId())
                .build());

        InquiryAnswer rootMessage = inquiryAnswerRepository.save(
                InquiryAnswer.createRoot(inquiry, request.getContent())
        );

        return InquiryDetailResponse.of(inquiry, List.of(rootMessage), resolveCultivationName(inquiry));
    }

    public Page<InquirySummaryResponse> getMyInquiries(Long userId, Pageable pageable) {
        return inquiryRepository.findAllByUserId(userId, pageable)
                .map(InquirySummaryResponse::from);
    }

    public InquiryDetailResponse getMyInquiryDetail(Long userId, Long inquiryId) {
        Inquiry inquiry = getInquiryOrThrow(inquiryId);
        requireOwner(inquiry, userId);

        List<InquiryAnswer> messages = inquiryAnswerRepository.findAllByInquiryIdOrderByCreatedAtAsc(inquiryId);
        return InquiryDetailResponse.of(inquiry, messages, resolveCultivationName(inquiry));
    }

    @Override
    @Transactional
    public InquiryDetailResponse addFollowUp(Long userId, Long inquiryId, InquiryMessageRequest request) {
        Inquiry inquiry = getInquiryOrThrow(inquiryId);
        requireOwner(inquiry, userId);

        InquiryAnswer latest = inquiryAnswerRepository.findTopByInquiryIdOrderByCreatedAtDesc(inquiryId)
                .orElseThrow(InquiryAnswerNotFoundException::new);

        inquiryAnswerRepository.save(InquiryAnswer.createFollowUp(inquiry, latest, request.getContent()));

        inquiry.markPending();

        List<InquiryAnswer> messages = inquiryAnswerRepository.findAllByInquiryIdOrderByCreatedAtAsc(inquiryId);
        return InquiryDetailResponse.of(inquiry, messages, resolveCultivationName(inquiry));
    }

    // 관리자 용
    @Override
    public Page<InquirySummaryResponse> getAllInquiries(Long adminUserId, InquiryStatus statusFilter, Pageable pageable) {
        requireAdmin(adminUserId);

        Page<Inquiry> page = statusFilter != null
                ? inquiryRepository.findAllByStatus(statusFilter, pageable)
                : inquiryRepository.findAll(pageable);

        return page.map(InquirySummaryResponse::from);
    }

    @Override
    public InquiryDetailResponse getInquiryDetailForAdmin(Long adminUserId, Long inquiryId) {
        requireAdmin(adminUserId);

        Inquiry inquiry = getInquiryOrThrow(inquiryId);
        List<InquiryAnswer> messages = inquiryAnswerRepository.findAllByInquiryIdOrderByCreatedAtAsc(inquiryId);
        return InquiryDetailResponse.of(inquiry, messages, resolveCultivationName(inquiry));
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
        return InquiryDetailResponse.of(inquiry, messages, resolveCultivationName(inquiry));
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

    private String resolveCultivationName(Inquiry inquiry) {
        if (inquiry.getCultivationId() == null) {
            return null;
        }
        try {
            CultivationSummaryResponse cultivation = cultivationClient.getCultivation(inquiry.getUserId(), inquiry.getCultivationId());
            return cultivation != null ? cultivation.name() : null;
        } catch (Exception e) {
            log.warn("경작지 정보 조회 실패 (inquiryId={}, cultivationId={}): {}", inquiry.getId(), inquiry.getCultivationId(), e.getMessage());
            return null;
        }
    }
}