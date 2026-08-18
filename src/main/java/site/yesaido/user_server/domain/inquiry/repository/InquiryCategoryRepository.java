package site.yesaido.user_server.domain.inquiry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.yesaido.user_server.domain.inquiry.entity.InquiryCategory;

@Repository
public interface InquiryCategoryRepository extends JpaRepository<InquiryCategory, Long> {
}
