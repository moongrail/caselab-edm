package ru.caselab.edm.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.caselab.edm.backend.dto.department.*;
import ru.caselab.edm.backend.dto.user.UserDTO;
import ru.caselab.edm.backend.dto.user.UserPageDTO;
import ru.caselab.edm.backend.entity.Department;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.exceptions.ManagerOfAnotherDepartmentException;
import ru.caselab.edm.backend.exceptions.NotDepartmentMemberException;
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

        log.info("Executing manager uuid");
        UUID id = UUID.fromString(createDepartmentDTO.manager());

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Manager with id %s doesn't exists".formatted(id)));

        System.out.println(userRepository.existsUserAsManager(id) + " " + id);
        if (userRepository.existsUserAsManager(id))
            throw new ManagerOfAnotherDepartmentException("User with id %s is already a manager of another department".formatted(id));

        if (createDepartmentDTO.parentId() != 0) {
            log.info("Getting parent department");
            Department parentDepartment = departmentRepository.findById(createDepartmentDTO.parentId()).orElseThrow(() -> new ResourceNotFoundException("Parent department with id %s doesn't exists".formatted(createDepartmentDTO.parentId())));

            if (!userRepository.existsUserInOtherDepartmentsAsMember(UUID.fromString(createDepartmentDTO.manager()))) {
                // Здесь get без проблем, т.к в верхних строчках мы проверяем существование этого пользователя
                log.info("Adding manager of current department to members of parent department");
                parentDepartment.getMembers().add(userRepository.findById(UUID.fromString(createDepartmentDTO.manager())).get());

                log.info("Saving parent department to repository");
                departmentRepository.save(parentDepartment);
                log.info("Parent department with id: {} saved", parentDepartment.getId());
            }
        }
        /*
            Обязательно ли руководитель должен быть в департаменте которым он руководит?
            Мб добавить роль MANAGER для каждого юзера являющегося руководителем?
         */

        log.info("Creating department entity");
        Department department = Department.builder().name(createDepartmentDTO.name())
                .description(createDepartmentDTO.description()).parentId(createDepartmentDTO.parentId())
                .manager(user).build();

        log.info("Saving department to repository");
        department = departmentRepository.save(department);
        log.info("Department with id: {} saved", department.getId());

        return departmentMapper.toDto(department);
    }


    @Override
    public DepartmentDTO getDepartmentWithUser(UUID id) {
        log.info("Fetching department with user id: {}", id);
        Department department = departmentRepository.getDepartmentWithUser(id).orElseThrow(() -> new NotDepartmentMemberException("You are not a member of any department"));
        return departmentMapper.toDto(department);
    }

    @Override
    public UserDTO getManagerOfDepartment(Long id) {
        log.info("Fetching manager with department id: {}",id);
        User manager = userRepository.getDepartmentManager(id).orElseThrow(() -> new ResourceNotFoundException("Department with id %s don't have any manager".formatted(id)));
        return userMapper.toDTO(manager);
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
        List<UUID> added = new ArrayList<>();

        for (UUID id : membersIds) {
            log.info("Getting user by user id: {}", id);
            Optional<User> user = userRepository.findById(id);
            if (user.isEmpty()) {
                log.info("User with id: {} doesn't exists", id);
                skipped.add(id);
            } else {
                User existingUser = user.get();

                log.info("User with id: {} exists", id);

                if (!userRepository.existsUserInOtherDepartmentsAsMember(id)) {
                    existingUser.setDepartment(existingDepartment);
                    existingDepartment.getMembers().add(existingUser);
                    added.add(id);
                    log.info("User with id: {} added to members list", id);
                    userRepository.save(existingUser);
                } else {
                    skipped.add(id);
                }
            }
        }

        log.info("Adding members list in members list of department entity");
        log.info("Updating department with id: {}", departmentId);
        departmentRepository.save(existingDepartment);
        log.info("Department with id: {} successfully updated", departmentId);

        return new StatisticMembersDTO(added, skipped);
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
                User existingUser = user.get();
                log.info("User with id: {} exists", id);
                if(!members.remove(user.get())) {
                    log.info("User with id: {} doesn't represent in department members list with department id: {}", id, departmentId);
                    skipped.add(id);
                } else {
                    existingUser.setDepartment(null);
                    userRepository.save(existingUser);
                    kicked.add(id);
                }
            }
        }

        log.info("Updating department with id: {}", departmentId);
        departmentRepository.save(existingDepartment);

        return new StatisticMembersDTO(kicked, skipped);
    }

    @Override
    public void leaveFromDepartment(UUID id, Long departmentId) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User with id %s was not found".formatted(id)));

        Department department = departmentRepository.findById(departmentId).orElseThrow(() -> new ResourceNotFoundException("Department with id %s not found".formatted(id)));

        log.info("Removing user with id: {} from members of department", id);
        department.getMembers().remove(user);

        if (department.getManager() == user) {
            log.info("Removing user with id: {} from managers of department", id);
            department.setManager(null);
        }

        user.setDepartment(null);
        user.setLeadDepartment(null);

        departmentRepository.save(department);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department with id %s not found".formatted(id)));

        if (department.getManager() != null) {
            User manager = department.getManager();
            manager.setLeadDepartment(null);
            department.setManager(null);
            userRepository.save(manager);
        }

        department.getMembers().forEach(user -> {
            if (user.getDepartment() != null && user.getDepartment().getId() == id) {
                user.setDepartment(null);
                if (user.getLeadDepartment() != null && user.getLeadDepartment().getId() == id) {
                    user.setLeadDepartment(null);
                }
                userRepository.save(user);
            }
        });

        department.setMembers(new HashSet<>());
        departmentRepository.flush();

        log.info("Trying to delete department with id: {}", id);
        departmentRepository.delete(department);
        departmentRepository.flush();

        log.info("Department with id: {} was successfully deleted", id);
    }


}
