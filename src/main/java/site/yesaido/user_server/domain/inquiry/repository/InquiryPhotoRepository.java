package site.yesaido.user_server.domain.inquiry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.yesaido.user_server.domain.inquiry.entity.InquiryPhoto;

import java.util.List;


@Repository
public interface InquiryPhotoRepository extends JpaRepository<InquiryPhoto, Long> {
    List<InquiryPhoto> findAllByInquiryAnswerId(Long inquiryAnswerId);
}
