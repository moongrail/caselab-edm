package ru.caselab.edm.backend.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.caselab.edm.backend.dto.approvementprocess.ApprovementProcessDTO;
import ru.caselab.edm.backend.dto.approvementprocess.ApprovementProcessResultDTO;
import ru.caselab.edm.backend.entity.ApprovementProcess;
import ru.caselab.edm.backend.entity.ApprovementProcessItem;
import ru.caselab.edm.backend.entity.Document;
import ru.caselab.edm.backend.entity.DocumentVersion;
import ru.caselab.edm.backend.mapper.approvementproccess.ApprovementProcessMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApprovementProcessMapperTest {

    private final ApprovementProcessMapper mapper = Mappers.getMapper(ApprovementProcessMapper.class);

    @Test
    void mapsDocumentIdAndItemsToProcessDto() {
        ApprovementProcess process = process();

        ApprovementProcessDTO result = mapper.toDTO(process);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getDocumentId()).isEqualTo(42L);
        assertThat(result.getAgreementPercent()).isEqualTo(60f);
        assertThat(result.getApprovementProcessItemsIds()).containsExactly(11L);
    }

    @Test
    void mapsResultDtoWithRealDocumentId() {
        ApprovementProcessResultDTO result = mapper.toResultDTO(process());

        assertThat(result.getDocumentId()).isEqualTo(42L);
        assertThat(result.getAgreementPercent()).isEqualTo(60f);
    }

    private ApprovementProcess process() {
        Document document = new Document();
        document.setId(42L);
        DocumentVersion version = DocumentVersion.builder()
                .id(7L)
                .document(document)
                .build();
        ApprovementProcessItem item = ApprovementProcessItem.builder()
                .id(11L)
                .build();
        return ApprovementProcess.builder()
                .id(3L)
                .documentVersion(version)
                .agreementProcent(60f)
                .approvementProcessItems(List.of(item))
                .build();
    }
}
