package ru.caselab.edm.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.caselab.edm.backend.dto.ApprovementProcessItemDTO;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;

@Mapper(componentModel = "spring")
public interface ApprovementProccessItemMapper {

    @Mapping(source = "approvementProcess.id", target = "approvementProcessId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "documentVersion.id", target = "documentVersionId")
    @Mapping(source = "signature.id", target = "signatureId")
    ApprovementProcessItemDTO toDTO(ApprovementProcessItem approvementProcessItem);
}
