package ru.caselab.edm.backend.mapper.approvementproccess;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.caselab.edm.backend.dto.approvementprocess.ApprovementProcessDTO;
import ru.caselab.edm.backend.dto.approvementprocess.ApprovementProcessResultDTO;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.DocumentVersion;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ApprovementProcessMapper {

    @Mapping(source = "approvementProcessItems", target = "approvementProcessItemsIds")
    @Mapping(source = "documentVersion.id", target = "documentId")
    @Mapping(source = "agreementProcent", target = "agreementPercent")
    ApprovementProcessDTO toDTO(ApprovementProcess approvementProcess);

    default List<Long> mapApprovementProcessItemsToIds(List<ApprovementProcessItem> approvementProcessItems) {
        return approvementProcessItems.stream()
                .map(ApprovementProcessItem::getId)
                .collect(Collectors.toList());
    }

    default Long mapDocumentVersionIdToDocumentId(DocumentVersion documentVersion){
        return documentVersion.getDocument().getId();
    }

    @Mapping(source = "documentVersion.id", target = "documentId")
    @Mapping(source = "agreementProcent", target = "agreementPercent")
    ApprovementProcessResultDTO toResultDTO(ApprovementProcess approvementProcess);
}
