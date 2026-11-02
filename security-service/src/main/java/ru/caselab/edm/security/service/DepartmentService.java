package ru.caselab.edm.security.service;

import ru.caselab.edm.security.api.dto.department.*;
import ru.caselab.edm.security.api.dto.user.UserDTO;
import ru.caselab.edm.security.api.dto.user.UserPageDTO;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {
    DepartmentDTO createDepartment(CreateDepartmentDTO createDepartmentDTO);
    List<DepartmentDTO> getDepartmentsWithUser(UUID id);
    UserDTO getManagerOfDepartment(Long id);
    UserPageDTO getAllMembersOfDepartment(int page, int size, Long id);
    DepartmentPageDTO getAllSubordinateDepartments(int page, int size, UUID id);
    StatisticMembersDTO addMembersToDepartment(MembersDTO addMembersDTO, Long departmentId);
    StatisticMembersDTO kickMembersFromDepartment(MembersDTO kickMembersDTO, Long departmentId);
    void leaveFromDepartment(UUID id, Long departmentId);
    void deleteDepartment(Long id);
}
