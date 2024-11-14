package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.replacementmanagement.UsersForReplacementDTO;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.mapper.user.UserMapper;
import ru.caselab.edm.backend.repository.DepartmentRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.ReplacementManagementService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReplacementManagementServiceImpl implements ReplacementManagementService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final UserMapper userMapper;

    @Override
    public Page<UsersForReplacementDTO> getAllUsersForReplacementManager(int page, int size, UUID userId) {
        log.info("Get department id where the manager with UUID: {} works ", userId);
        List<Long> departmentByManagerUuid = departmentRepository.findDepartmentByManagerUuid(userId);
        List<UsersForReplacementDTO> allUsersForReplacementManager = new ArrayList<>();

        for (Long d : departmentByManagerUuid) {
            List<User> departmentMembersForReplacementManager = userRepository.getDepartmentMembersForReplacementManager(d);
            UsersForReplacementDTO usersForReplacementDTO = new UsersForReplacementDTO();
            usersForReplacementDTO.setDepartmentId(d);
            usersForReplacementDTO.setUsers(userMapper.toListDTO(departmentMembersForReplacementManager));
            allUsersForReplacementManager.add(usersForReplacementDTO);
        }
        return listMapToPage(page, size, allUsersForReplacementManager);
    }

    @Override
    public Page<UsersForReplacementDTO> getAllUsersForReplacementDepartmentMember(int page, int size, UUID userId) {
        log.info("Get department id where the user with UUID: {} works ", userId);
        List<Long> departmentIdByMemberUuid = departmentRepository.findDepartmentByMemberUuid(userId);
        List<UsersForReplacementDTO> allUsersForReplacementDepartmentMember = new ArrayList<>();

        for (Long d : departmentIdByMemberUuid) {
            List<User> departmentManagersForReplacementDepartmentMember = userRepository.getDepartmentManagersForReplacementDepartmentMember(d);
            List<User> departmentMembersForReplacement = userRepository.getDepartmentMembersForReplacement(userId, d);
            List<User> departmentMembersAndManagersForReplacement = (List<User>) CollectionUtils.union(departmentManagersForReplacementDepartmentMember, departmentMembersForReplacement);

            UsersForReplacementDTO usersForReplacementDTO = new UsersForReplacementDTO();
            usersForReplacementDTO.setDepartmentId(d);
            usersForReplacementDTO.setUsers(userMapper.toListDTO(departmentMembersAndManagersForReplacement));
            allUsersForReplacementDepartmentMember.add(usersForReplacementDTO);
        }

        return listMapToPage(page, size, allUsersForReplacementDepartmentMember);
    }

    private static PageImpl<UsersForReplacementDTO> listMapToPage(int page, int size, List<UsersForReplacementDTO> allUsersForReplacementManager) {
        PageRequest pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allUsersForReplacementManager.size());

        allUsersForReplacementManager.subList(start, end);
        return new PageImpl<>(allUsersForReplacementManager, pageable, allUsersForReplacementManager.size());
    }
}
