package ru.caselab.edm.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.caselab.edm.backend.dto.ApprovementProcessDTO;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
interface ApprovementProcessMapper {

    @Mapping(source = "approvementProcessItems", target = "approvementProcessItemsIds")
    @Mapping(source = "documentVersion.id", target = "documentVersionId")
    ApprovementProcessDTO toDTO(ApprovementProcess approvementProcess);

    default List<Long> mapApprovementProcessItemsToIds(List<ApprovementProcessItem> approvementProcessItems) {
        return approvementProcessItems.stream()
                .map(ApprovementProcessItem::getId)
                .collect(Collectors.toList());
    }
}
