package org.sopt.android1.domain.record.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.sopt.android1.domain.record.dto.request.RecordCreateRequest;
import org.sopt.android1.domain.record.dto.response.RecordCreateResponse;
import org.sopt.android1.domain.record.dto.response.RecordDetailResponse;
import org.sopt.android1.domain.record.entity.RecordEntity;
import org.sopt.android1.domain.record.repository.RecordRepository;
import org.sopt.android1.global.exception.BusinessException;
import org.sopt.android1.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.sopt.android1.domain.record.response.HomeRecordsResponse;
import org.sopt.android1.domain.user.entity.UserEntity;
import org.sopt.android1.domain.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class RecordService {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("M'월' d'일'", Locale.KOREAN);
    private static final DateTimeFormatter TIME_LABEL_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Map<String, String> REGION_SHORT = Map.ofEntries(
            Map.entry("서울특별시", "서울"),
            Map.entry("서울시", "서울"),
            Map.entry("부산광역시", "부산"),
            Map.entry("부산시", "부산"),
            Map.entry("대구광역시", "대구"),
            Map.entry("대구시", "대구"),
            Map.entry("인천광역시", "인천"),
            Map.entry("인천시", "인천"),
            Map.entry("광주광역시", "광주"),
            Map.entry("광주시", "광주"),
            Map.entry("대전광역시", "대전"),
            Map.entry("대전시", "대전"),
            Map.entry("울산광역시", "울산"),
            Map.entry("울산시", "울산"),
            Map.entry("세종특별자치시", "세종"),
            Map.entry("경기도", "경기"),
            Map.entry("강원도", "강원"),
            Map.entry("강원특별자치도", "강원"),
            Map.entry("충청북도", "충북"),
            Map.entry("충청남도", "충남"),
            Map.entry("전라북도", "전북"),
            Map.entry("전북특별자치도", "전북"),
            Map.entry("전라남도", "전남"),
            Map.entry("경상북도", "경북"),
            Map.entry("경상남도", "경남"),
            Map.entry("제주특별자치도", "제주")
    );

    private final RecordRepository recordRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Value("${app.public-url-prefix}")
    private String publicUrlPrefix;

    @Transactional(readOnly = true)
    public RecordDetailResponse getDetail(Long recordId) {
        RecordEntity entity = recordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        LocalDateTime recordedAt = entity.getRecordedAt();
        OffsetDateTime createdAt = entity.getCreatedAt().atZone(KST).toOffsetDateTime();
        return RecordDetailResponse.of(
                entity,
                DATE_LABEL_FORMATTER.format(recordedAt),
                TIME_LABEL_FORMATTER.format(recordedAt),
                shortenLocation(entity.getLocation()),
                formatVoiceDuration(entity.getVoiceDurationSeconds()),
                createdAt
        );
    }

    @Transactional
    public RecordCreateResponse create(RecordCreateRequest request) {
        String photoUrl = storePhoto(request.photo());

        RecordEntity entity = RecordEntity.builder()
                .userId(DEFAULT_USER_ID)
                .title(request.title())
                .photoUrl(photoUrl)
                .voiceDurationSeconds(request.voiceDurationSeconds())
                .isShared(Boolean.TRUE.equals(request.isShared()))
                .location(request.location())
                .recordedAt(request.recordedAt().atZoneSameInstant(KST).toLocalDateTime())
                .build();

        RecordEntity saved = recordRepository.save(entity);
        OffsetDateTime recordedAt = saved.getRecordedAt().atZone(KST).toOffsetDateTime();
        OffsetDateTime createdAt = saved.getCreatedAt().atZone(KST).toOffsetDateTime();
        return RecordCreateResponse.of(saved, recordedAt, createdAt);
    }

    private String storePhoto(MultipartFile photo) {
        if (photo == null || photo.isEmpty()) {
            return null;
        }

        try {
            Path directory = Path.of(uploadDir);
            Files.createDirectories(directory);

            String extension = resolveExtension(photo.getOriginalFilename());
            String filename = UUID.randomUUID() + extension;
            Path target = directory.resolve(filename);
            photo.transferTo(target.toAbsolutePath());

            String prefix = publicUrlPrefix.endsWith("/")
                    ? publicUrlPrefix.substring(0, publicUrlPrefix.length() - 1)
                    : publicUrlPrefix;
            return prefix + "/" + filename;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private String resolveExtension(String originalFilename) {
        String ext = StringUtils.getFilenameExtension(originalFilename);
        return ext == null || ext.isBlank() ? "" : "." + ext;
    }

    private String shortenLocation(String location) {
        if (location == null || location.isBlank()) {
            return "";
        }
        String[] tokens = location.trim().split("\\s+");
        String first = REGION_SHORT.getOrDefault(tokens[0], tokens[0]);
        if (tokens.length == 1) {
            return first;
        }
        return first + " " + tokens[1];
    }

    private String formatVoiceDuration(Integer seconds) {
        if (seconds == null) {
            return null;
        }
        int minutes = seconds / 60;
        int remain = seconds % 60;
        return String.format("%d:%02d", minutes, remain);
    }

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public HomeRecordsResponse getSharedRecords() {
        var records = recordRepository.findAllByIsSharedTrueOrderByCreatedAtDesc();
        var usersById = getUsersById(records);

        validateAuthorsExist(records, usersById);

        return HomeRecordsResponse.from(records, usersById);
    }

    private Map<Long, UserEntity> getUsersById(List<RecordEntity> records) {
        var userIds = records.stream()
            .map(RecordEntity::getUserId)
            .distinct()
            .toList();

        return userRepository.findAllById(userIds).stream()
            .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
    }

    private void validateAuthorsExist(
        List<RecordEntity> records,
        Map<Long, UserEntity> usersById
    ) {
        records.stream()
            .map(RecordEntity::getUserId)
            .filter(userId -> !usersById.containsKey(userId))
            .findFirst()
            .ifPresent(userId -> {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "userId=" + userId);
            });
    }
}
