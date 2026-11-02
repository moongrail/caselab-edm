package ru.caselab.edm.backend.approval.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.caselab.edm.backend.approval.api.dto.approvementprocessitem.ApprovementProcessItemDTO;
import ru.caselab.edm.backend.approval.domain.ApprovementProcessItem;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApprovementProccessItemMapper {

    @Mapping(source = "approvementProcess.id", target = "approvementProcessId")
    @Mapping(source = "signerId", target = "userId")
    @Mapping(source = "documentVersion.id", target = "documentVersionId")
    @Mapping(source = "signature.id", target = "signatureId")
    ApprovementProcessItemDTO toDTO(ApprovementProcessItem approvementProcessItem);

    @Mapping(source = "approvementProcessId", target = "approvementProcess.id")
    @Mapping(source = "userId", target = "signerId")
    @Mapping(source = "documentVersionId", target = "documentVersion.id")
    @Mapping(source = "signatureId", target = "signature.id")
    ApprovementProcessItem toEntity(ApprovementProcessItemDTO approvementProcessItemDTO);

    List<ApprovementProcessItem> toEntityList(List<ApprovementProcessItemDTO> approvementProcessItemDTOs);
}
