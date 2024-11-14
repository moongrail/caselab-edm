package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.caselab.edm.backend.dto.department.*;
import ru.caselab.edm.backend.dto.user.UserPageDTO;
import ru.caselab.edm.backend.entity.Department;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.exceptions.ResourceNotFoundException;
import ru.caselab.edm.backend.mapper.department.DepartmentMapper;
import ru.caselab.edm.backend.mapper.user.UserMapper;
import ru.caselab.edm.backend.repository.DepartmentRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.service.DepartmentService;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final DepartmentMapper departmentMapper;
    private final UserMapper userMapper;

    @Override
    public DepartmentDTO createDepartment(CreateDepartmentDTO createDepartmentDTO) {
        Set<User> managers = new HashSet<>();

        log.info("Executing all managers uuid to managers User list");
        for (UUID id : createDepartmentDTO.managers()) {
            Optional<User> user = userRepository.findById(id);

            if (user.isEmpty()) {
                /*
                Думаю нет смысла делать так, чтобы абсолютно все руководители из списка существовали,
                просто нужен как минимум 1 руководитель на департамент
                 */
                log.info("User with id: {} does not exists", id);
            } else {
                User existingUser = user.get();
                /*
                Обязательно ли руководитель должен быть в департаменте которым он руководит?
                Мб добавить роль MANAGER для каждого юзера являющегося руководителем?
                 */

                log.info("User with id: {} added to managers list", id);
                managers.add(existingUser);
            }
        }

        if (managers.isEmpty())
            throw new ResourceNotFoundException("Managers are not exists");

        log.info("Creating department entity");
        Department department = Department.builder().name(createDepartmentDTO.name())
                .description(createDepartmentDTO.description()).parentId(createDepartmentDTO.parentId())
                .managers(managers).build();

        log.info("Saving department to repository");
        department = departmentRepository.save(department);
        log.info("Department with id: {} saved", department.getId());

        return departmentMapper.toDto(department);
    }


    @Override
    public DepartmentPageDTO getAllDepartmentsWithUser(int page, int size, UUID id) {
        log.info("Fetching all departments - page: {}, size: {}, with user id: {}", page, size, id);
        Page<Department> departments = departmentRepository.getDepartmentsWithUser(id, PageRequest.of(page, size));
        log.info("Fetched {} departments", departments.getTotalElements());
        return departmentMapper.toPageDTO(departments);
    }

    @Override
    public UserPageDTO getAllManagersDepartment(int page, int size, Long id) {
        log.info("Fetching all managers - page: {}, size: {}, with department id: {}", page, size, id);
        Page<User> managers = userRepository.getDepartmentManagers(id, PageRequest.of(page, size));
        log.info("Fetched {} managers", managers.getTotalElements());
        return userMapper.toPageDTO(managers);
    }

    @Override
    public UserPageDTO getAllMembersOfDepartment(int page, int size, Long id) {
        log.info("Fetching all members - page: {}, size: {}, with department id: {}", page, size, id);
        Page<User> members = userRepository.getDepartmentMembers(id, PageRequest.of(page, size));
        log.info("Fetched {} members", members.getTotalElements());
        return userMapper.toPageDTO(members);
    }

    @Override
    public DepartmentPageDTO getAllSubordinateDepartments(int page, int size, UUID id) {
        log.info("Fetching all subordinate departments - page: {}, size: {}, with user id: {}", page, size, id);
        Page<Department> departments = departmentRepository.getSubordinateDepartments(id, PageRequest.of(page, size));
        log.info("Fetched {} subordinate departments", departments.getTotalElements());
        return departmentMapper.toPageDTO(departments);
    }

    @Override
    public StatisticMembersDTO addMembersToDepartment(MembersDTO addMembersDTO, Long departmentId) {
        Optional<Department> department = departmentRepository.findById(departmentId);
        if (department.isEmpty())
            throw new ResourceNotFoundException("Department not found with id = %s".formatted(department));

        Department existingDepartment = department.get();

        List<UUID> membersIds = addMembersDTO.members();
        List<UUID> skipped = new ArrayList<>();
        List<User> members = new ArrayList<>();

        for (UUID id : membersIds) {
            log.info("Getting user by user id: {}", id);
            Optional<User> user = userRepository.findById(id);
            if (user.isEmpty()) {
                log.info("User with id: {} doesn't exists", id);
                skipped.add(id);
            } else {
                log.info("User with id: {} exists", id);
                members.add(user.get());
                log.info("User with id: {} added to members list", id);
            }
        }

        log.info("Adding members list in members list of department entity");
        existingDepartment.getMembers().addAll(members);
        log.info("Updating department with id: {}", departmentId);
        departmentRepository.save(existingDepartment);
        log.info("Department with id: {} successfully updated", departmentId);

        return new StatisticMembersDTO(membersIds, skipped);
    }

    @Override
    public StatisticMembersDTO kickMembersFromDepartment(MembersDTO kickMembersDTO, Long departmentId) {
        Optional<Department> department = departmentRepository.findById(departmentId);
        if (department.isEmpty())
            throw new ResourceNotFoundException("Department not found with id = %s".formatted(department));

        Department existingDepartment = department.get();

        Set<User> members = existingDepartment.getMembers();
        List<UUID> kicked = new ArrayList<>();
        List<UUID> skipped = new ArrayList<>();

        for (UUID id : kickMembersDTO.members()) {
            log.info("Getting user with id: {}", id);
            Optional<User> user = userRepository.findById(id);

            if (user.isEmpty()) {
                log.info("User with id: {} doesn't exists", id);
                skipped.add(id);
            } else {
                log.info("User with id: {} exists", id);
                if(!members.remove(user.get())) {
                    log.info("User with id: {} doens't represent in department members list with department id: {}", id, departmentId);
                    skipped.add(id);
                }
                kicked.add(id);
            }
        }

        log.info("Updating department with id: {}", departmentId);
        departmentRepository.save(existingDepartment);

        return new StatisticMembersDTO(kicked, skipped);
    }

    @Override
    public void deleteDepartment(Long id) {
        log.info("Trying to delete department with id: {}", id);
        departmentRepository.deleteById(id);
        log.info("Department with id: {} was successfully deleted", id);
    }
}
