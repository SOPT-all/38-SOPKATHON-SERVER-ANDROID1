package org.sopt.android1.domain.record.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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

@Service
@RequiredArgsConstructor
public class RecordService {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final RecordRepository recordRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Value("${app.public-url-prefix}")
    private String publicUrlPrefix;

    @Transactional(readOnly = true)
    public RecordDetailResponse getDetail(Long recordId) {
        RecordEntity entity = recordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        OffsetDateTime createdAt = entity.getCreatedAt().atZone(KST).toOffsetDateTime();
        return RecordDetailResponse.of(entity, createdAt);
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
}