package site.yesaido.user_server.domain.inquiry.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import site.yesaido.user_server.domain.inquiry.exception.InquiryAnswerThreadMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "answer_content", columnDefinition = "TEXT")
    private String answerContent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pre_id")
    private InquiryAnswer pre;

    @Builder.Default
    @OneToMany(mappedBy = "inquiryAnswer", fetch = FetchType.LAZY)
    private List<InquiryPhoto> inquiryPhotos = new ArrayList<>();

    public void answer(String answerContent) {
        this.answerContent = answerContent;
    }

    // 문의 등록 시 최초 질문 생성
    public static InquiryAnswer createRoot(Inquiry inquiry, String content) {
        return InquiryAnswer.builder()
                .inquiry(inquiry)
                .content(content)
                .build();
    }

    // 추가 질문 생성
    public static InquiryAnswer createFollowUp(Inquiry inquiry, InquiryAnswer pre, String content) {
        validateSameInquiry(inquiry, pre);
        return InquiryAnswer.builder()
                .inquiry(inquiry)
                .pre(pre)
                .content(content)
                .build();
    }

    private static void validateSameInquiry(Inquiry inquiry, InquiryAnswer pre) {
        if (pre != null && !Objects.equals(pre.getInquiry().getId(), inquiry.getId())) {
            throw new InquiryAnswerThreadMismatchException("pre(id=%d)는 다른 문의(inquiryId=%d)에 속해 있어 현재 문의(inquiryId=%d)의 이전 메시지로 연결할 수 없습니다.".formatted(pre.getId(), pre.getInquiry().getId(), inquiry.getId()));
        }
    }

    // flush 시점에 무조건 걸림. 저장하기 전에 한번더 같은 문의인지 확인함.
    @PrePersist
    @PreUpdate
    private void validateBeforeSave() {
        validateSameInquiry(this.inquiry, this.pre);
    }

    public void addInquiryPhoto(InquiryPhoto photo){
        this.inquiryPhotos.add(photo);
    }
}
