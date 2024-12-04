package ru.caselab.edm.backend.testutils.builder.department;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;
import ru.caselab.edm.backend.entity.Department;
import ru.caselab.edm.backend.entity.User;
import ru.caselab.edm.backend.testutils.builder.BaseBuilder;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor(staticName = "builder")
@With
public class DepartmentEntityBuilder implements BaseBuilder<Department> {

    private String name = "test-name";
    private String description = "test-descr";
    private Long parentId = null;
    private Set<User> members = new HashSet<>();
    private User manager = null;

    @Override
    public Department build() {
        return Department.builder()
                .name(name)
                .description(description)
                .parentId(parentId)
                .members(members)
                .manager(manager)
                .build();
    }
}
