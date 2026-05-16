package org.sopt.android1.domain.record.response;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.sopt.android1.domain.record.entity.RecordEntity;
import org.sopt.android1.domain.user.entity.UserEntity;

public record HomeRecordResponse(
    Long recordId,
    HomeAuthorResponse author,
    String title,
    String photoUrl,
    Integer voiceDurationSeconds,
    String createdAt
) {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter ISO_OFFSET_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static HomeRecordResponse from(RecordEntity record, UserEntity author) {
        return new HomeRecordResponse(
            record.getId(),
            HomeAuthorResponse.from(author),
            record.getTitle(),
            record.getPhotoUrl(),
            record.getVoiceDurationSeconds(),
            record.getCreatedAt().atZone(SEOUL_ZONE).format(ISO_OFFSET_FORMATTER)
        );
    }
}
