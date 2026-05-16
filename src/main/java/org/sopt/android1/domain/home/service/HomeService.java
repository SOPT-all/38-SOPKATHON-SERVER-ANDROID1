package org.sopt.android1.domain.home.service;

import lombok.RequiredArgsConstructor;
import org.sopt.android1.domain.home.dto.response.HomeRecordsResponse;
import org.sopt.android1.domain.record.repository.RecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final RecordRepository recordRepository;

    @Transactional(readOnly = true)
    public HomeRecordsResponse getSharedRecords() {
        return HomeRecordsResponse.from(recordRepository.findAllByIsSharedTrueOrderByCreatedAtDesc());
    }
}
