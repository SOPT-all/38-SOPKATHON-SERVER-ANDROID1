package org.sopt.android1.domain.record.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.sopt.android1.domain.record.entity.RecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {

    List<RecordEntity> findAllByIsSharedTrueOrderByCreatedAtDesc();

    List<RecordEntity> findAllByUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
            Long userId,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );
}
