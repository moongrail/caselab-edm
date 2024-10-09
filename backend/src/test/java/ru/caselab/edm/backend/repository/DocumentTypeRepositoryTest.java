package ru.caselab.edm.backend.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.caselab.edm.backend.entity.DocumentAttribute;
import ru.caselab.edm.backend.entity.DocumentType;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@ActiveProfiles("test")
@DataJpaTest
class DocumentTypeRepositoryTest {
    @Autowired
    DocumentTypeRepository documentTypeRepository;
    @Autowired
    AttributesRepository attributesRepository;

    @Test
    void findByName() {
        LocalDateTime now = LocalDateTime
                .of(2024, Month.FEBRUARY, 22, 9, 49, 19, 275039200);

        DocumentAttribute documentAttribute = new DocumentAttribute();
        documentAttribute.setName("подписант");
        documentAttribute.setDataType("текст");
        attributesRepository.saveAndFlush(documentAttribute);

        List<DocumentAttribute> documentAttributeList = new ArrayList<>();
        documentAttributeList.add(documentAttribute);

        DocumentType documentType = new DocumentType();
        documentType.setId(1L);
        documentType.setName("договор");
        documentType.setDescription("какоей-то описание");
        documentType.setCreatedAt(now);
        documentType.setAttributes(documentAttributeList);

        documentTypeRepository.save(documentType);

        Assertions.assertThat(documentTypeRepository.findByName("договор").get().getAttributes().get(0))
                .isEqualTo(documentType.getAttributes().get(0));

        Assertions.assertThat(documentTypeRepository.findByName("договор").get())
                .matches(list -> list.getId().equals(documentType.getId()))
                .matches(list -> list.getName().equals(documentType.getName()))
                .matches(list -> list.getDescription().equals(documentType.getDescription()))
                .matches(list -> list.getCreatedAt().equals(documentType.getCreatedAt()))
                .matches(list -> list.getAttributes().get(0).equals(documentType.getAttributes().get(0)));
    }
}