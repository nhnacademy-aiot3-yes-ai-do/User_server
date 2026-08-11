package site.yesaido.user_server.domain.inquiry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.yesaido.user_server.domain.inquiry.entity.InquiryAnswer;

import java.util.List;
import java.util.Optional;

@Repository
public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {
    List<InquiryAnswer> findAllByInquiryIdOrderByCreatedAtAsc(Long userId);
    Optional<InquiryAnswer> findTopByInquiryIdOrderByCreatedAtDesc(Long id);
}
