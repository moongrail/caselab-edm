package ru.caselab.edm.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.caselab.edm.backend.dto.ApprovementProcessItemDTO;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApprovementProccessItemMapper {

    @Mapping(source = "approvementProcess.id", target = "approvementProcessId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "documentVersion.id", target = "documentVersionId")
    @Mapping(source = "signature.id", target = "signatureId")
    ApprovementProcessItemDTO toDTO(ApprovementProcessItem approvementProcessItem);

    @Mapping(source = "approvementProcessId", target = "approvementProcess.id")
    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "documentVersionId", target = "documentVersion.id")
    @Mapping(source = "signatureId", target = "signature.id")
    ApprovementProcessItem toEntity(ApprovementProcessItemDTO approvementProcessItemDTO);

    List<ApprovementProcessItem> toEntityList(List<ApprovementProcessItemDTO> approvementProcessItemDTOs);
}
