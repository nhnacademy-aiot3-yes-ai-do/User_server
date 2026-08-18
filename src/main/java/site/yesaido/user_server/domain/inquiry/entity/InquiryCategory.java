package site.yesaido.user_server.domain.inquiry.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inquiry_category")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    public void updateCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public static InquiryCategory create(String categoryName){
        InquiryCategory category = new InquiryCategory();
        category.categoryName = categoryName;
        return category;
    }
}
