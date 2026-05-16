package org.sopt.android1.domain.home.dto.response;

import java.util.List;
import org.sopt.android1.domain.record.entity.RecordEntity;

public record HomeRecordsResponse(
    List<HomeRecordResponse> records
) {

    public static HomeRecordsResponse from(List<RecordEntity> records) {
        return new HomeRecordsResponse(
            records.stream()
                .map(HomeRecordResponse::from)
                .toList()
        );
    }
}
