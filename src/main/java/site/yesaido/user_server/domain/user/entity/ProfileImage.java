package site.yesaido.user_server.domain.user.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "profile_image")
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


}
