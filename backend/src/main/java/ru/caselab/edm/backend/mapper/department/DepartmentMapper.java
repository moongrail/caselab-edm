package ru.caselab.edm.backend.mapper.department;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import ru.caselab.edm.backend.dto.department.DepartmentDTO;
import ru.caselab.edm.backend.dto.department.DepartmentPageDTO;
import ru.caselab.edm.backend.entity.Department;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {
    DepartmentDTO toDto(Department department);
    @Mapping(target = "page", source = "number")
    DepartmentPageDTO toPageDTO(Page<Department> departments);

}
