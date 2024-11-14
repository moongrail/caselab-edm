package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.replacementmanagement.UsersForReplacementDTO;
import ru.caselab.edm.backend.entity.Department;
import ru.caselab.edm.backend.entity.User;
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

    @Override
    public Page<UsersForReplacementDTO> getAllUsersForReplacementManager(int page, int size, UUID userId) {
        log.info("Get department id where the manager with UUID: {} works ", userId);
        List<Department> departmentByManagerUuid = departmentRepository.findDepartmentByManagerUuid(userId);
        List<UsersForReplacementDTO> allUsersForReplacementManager = new ArrayList<>();

        for (Department d : departmentByManagerUuid) {
            List<User> departmentMembersForReplacementManager = userRepository.getDepartmentMembersForReplacementManager(d.getId());
            for (User u : departmentMembersForReplacementManager) {
                UsersForReplacementDTO usersForReplacementDTO = mapToDTO(d, u);
                allUsersForReplacementManager.add(usersForReplacementDTO);
            }
        }

        return listMapToPage(page, size, allUsersForReplacementManager);
    }

    @Override
    public Page<UsersForReplacementDTO> getAllUsersForReplacementDepartmentMember(int page, int size, UUID userId) {
        log.info("Get department id where the user with UUID: {} works ", userId);
        List<Department> departmentByMemberUuid = departmentRepository.findDepartmentByMemberUuid(userId);
        List<UsersForReplacementDTO> allUsersForReplacementDepartmentMember = new ArrayList<>();

        for (Department d : departmentByMemberUuid) {
            List<User> departmentManagersForReplacementDepartmentMember = userRepository.getDepartmentManagersForReplacementDepartmentMember(d.getId());
            List<User> departmentMembersForReplacement = userRepository.getDepartmentMembersForReplacement(userId, d.getId());
            List<User> departmentMembersAndManagersForReplacement = (List<User>) CollectionUtils.union(departmentManagersForReplacementDepartmentMember, departmentMembersForReplacement);

            for (User u : departmentMembersAndManagersForReplacement) {
                UsersForReplacementDTO usersForReplacementDTO = mapToDTO(d, u);
                allUsersForReplacementDepartmentMember.add(usersForReplacementDTO);
            }
        }

        return listMapToPage(page, size, allUsersForReplacementDepartmentMember);
    }

    private static UsersForReplacementDTO mapToDTO(Department d, User u) {
        UsersForReplacementDTO usersForReplacementDTO = new UsersForReplacementDTO();
        usersForReplacementDTO.setId(u.getId());
        usersForReplacementDTO.setLogin(u.getLogin());
        usersForReplacementDTO.setEmail(u.getEmail());
        usersForReplacementDTO.setPatronymic(u.getPatronymic());
        usersForReplacementDTO.setPosition(u.getPosition());
        usersForReplacementDTO.setFirstName(u.getFirstName());
        usersForReplacementDTO.setLastName(u.getLastName());
        usersForReplacementDTO.setDepartmentId(d.getId());
        return usersForReplacementDTO;
    }

    private static PageImpl<UsersForReplacementDTO> listMapToPage(int page, int size, List<UsersForReplacementDTO> allUsersForReplacementManager) {
        PageRequest pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allUsersForReplacementManager.size());

        allUsersForReplacementManager.subList(start, end);
        return new PageImpl<>(allUsersForReplacementManager, pageable, allUsersForReplacementManager.size());
    }
}
