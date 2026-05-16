package org.sopt.android1.domain.record.response;

import java.util.List;
import java.util.Map;
import org.sopt.android1.domain.record.entity.RecordEntity;
import org.sopt.android1.domain.user.entity.UserEntity;

public record HomeRecordsResponse(
    List<HomeRecordResponse> records
) {

    public static HomeRecordsResponse from(List<RecordEntity> records, Map<Long, UserEntity> usersById) {
        return new HomeRecordsResponse(
            records.stream()
                .map(record -> HomeRecordResponse.from(record, usersById.get(record.getUserId())))
                .toList()
        );
    }
}
