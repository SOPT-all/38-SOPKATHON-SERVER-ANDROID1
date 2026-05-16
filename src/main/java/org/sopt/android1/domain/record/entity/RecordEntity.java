package org.sopt.android1.domain.record.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.android1.global.persistence.BaseEntity;

@Entity
@Getter
@Table(
        name = "record",
        indexes = {
                @Index(name = "idx_record_is_shared_created_at", columnList = "is_shared, created_at"),
                @Index(name = "idx_record_user_id_created_at", columnList = "user_id, created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecordEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "voice_duration_seconds")
    private Integer voiceDurationSeconds;

    @Column(name = "is_shared", nullable = false)
    private boolean isShared;

    @Builder
    private RecordEntity(
            Long userId,
            String title,
            String photoUrl,
            Integer voiceDurationSeconds,
            boolean isShared
    ) {
        this.userId = userId;
        this.title = title;
        this.photoUrl = photoUrl;
        this.voiceDurationSeconds = voiceDurationSeconds;
        this.isShared = isShared;
    }
}
