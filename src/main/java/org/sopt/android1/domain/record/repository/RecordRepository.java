package org.sopt.android1.domain.record.repository;

import java.util.List;
import org.sopt.android1.domain.record.entity.RecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<RecordEntity, Long> {

    List<RecordEntity> findAllByIsSharedTrueOrderByCreatedAtDesc();
}
