package ru.caselab.edm.backend.mapper.department;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.department.DepartmentDTO;
import ru.caselab.edm.backend.dto.department.DepartmentPageDTO;
import ru.caselab.edm.backend.entity.Department;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {
    DepartmentDTO toDto(Department department);
    DepartmentPageDTO toPageDTO(Page<Department> departments);

}
