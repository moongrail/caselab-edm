package ru.caselab.edm.backend.service;

import ru.caselab.edm.backend.dto.department.*;
import ru.caselab.edm.backend.dto.user.UserDTO;
import ru.caselab.edm.backend.dto.user.UserPageDTO;

import java.util.UUID;

public interface DepartmentService {
    DepartmentDTO createDepartment(CreateDepartmentDTO createDepartmentDTO);
    DepartmentDTO getDepartmentWithUser(UUID id);
    UserDTO getManagerOfDepartment(Long id);
    UserPageDTO getAllMembersOfDepartment(int page, int size, Long id);
    DepartmentPageDTO getAllSubordinateDepartments(int page, int size, UUID id);
    StatisticMembersDTO addMembersToDepartment(MembersDTO addMembersDTO, Long departmentId);
    StatisticMembersDTO kickMembersFromDepartment(MembersDTO kickMembersDTO, Long departmentId);
    void leaveFromDepartment(UUID id, Long departmentId);
    void deleteDepartment(Long id);
}
