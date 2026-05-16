package org.sopt.android1.domain.record.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.sopt.android1.domain.record.entity.RecordEntity;
import org.sopt.android1.domain.record.repository.RecordRepository;
import org.sopt.android1.domain.record.response.HomeRecordsResponse;
import org.sopt.android1.domain.user.entity.UserEntity;
import org.sopt.android1.domain.user.repository.UserRepository;
import org.sopt.android1.global.exception.BusinessException;
import org.sopt.android1.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;
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
