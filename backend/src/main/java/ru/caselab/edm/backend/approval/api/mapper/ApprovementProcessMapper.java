package ru.caselab.edm.backend.approval.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.caselab.edm.backend.approval.api.dto.approvementprocess.ApprovementProcessDTO;
import ru.caselab.edm.backend.approval.api.dto.approvementprocess.ApprovementProcessResultDTO;
import ru.caselab.edm.backend.approval.domain.ApprovementProcess;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItem;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ApprovementProcessMapper {

    @Mapping(source = "approvementProcessItems", target = "approvementProcessItemsIds")
    @Mapping(source = "documentVersion.document.id", target = "documentId")
    @Mapping(source = "agreementProcent", target = "agreementPercent")
    ApprovementProcessDTO toDTO(ApprovementProcess approvementProcess);

    default List<Long> mapApprovementProcessItemsToIds(List<ApprovementProcessItem> approvementProcessItems) {
        return approvementProcessItems.stream()
                .map(ApprovementProcessItem::getId)
                .collect(Collectors.toList());
    }

    @Mapping(source = "documentVersion.document.id", target = "documentId")
    @Mapping(source = "agreementProcent", target = "agreementPercent")
    ApprovementProcessResultDTO toResultDTO(ApprovementProcess approvementProcess);
}
