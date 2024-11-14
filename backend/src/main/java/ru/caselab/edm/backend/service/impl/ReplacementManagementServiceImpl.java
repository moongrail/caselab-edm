package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.ReplacementManagementService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplacementManagementServiceImpl implements ReplacementManagementService {
    private final UserRepository userRepository;

    @Override
    public Page<User> getAllUsersForReplacement(int page, int size, UUID userId) {
        PageRequest pageable = PageRequest.of(page, size);
        log.info("Get a page of users available for replacement - page: {}, size: {}", page, size);
        return userRepository.getAllUsersForReplacement(userId, pageable);
    }
}
