package site.yesaido.user_server.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profile_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "object_key", length = 500, nullable = false)
    private String objectKey;

    @Column(name = "storage_type", length = 50, nullable = false)
    private String storageType;

    public static ProfileImage create(User user, String objectKey){
        ProfileImage profileImage = new ProfileImage();
        profileImage.user = user;
        profileImage.objectKey = objectKey;
        profileImage.storageType = "MINIO";
        return profileImage;
    }

    public void updateObjectKey(String objectKey){
        this.objectKey = objectKey;
    }
}
