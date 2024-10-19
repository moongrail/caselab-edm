package ru.caselab.edm.backend.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ActiveProfiles("test")
@DataJpaTest
class DocumentTypeRepositoryTest {
    @Autowired
    DocumentTypeRepository documentTypeRepository;
    @Autowired
    AttributeRepository attributesRepository;

    @Test
    void findByName() {
        Attribute attribute = new Attribute();
        attribute.setName("подписант");
        attribute.setDataType("текст");
        attribute.setRequired(true);
        attributesRepository.saveAndFlush(attribute);

        Set<Attribute> documentAttributeList = new HashSet<>();
        documentAttributeList.add(attribute);

        DocumentType documentType = new DocumentType();
        documentType.setId(1L);
        documentType.setName("договор");
        documentType.setDescription("какоей-то описание");
        documentType.setAttributes(documentAttributeList);

        documentTypeRepository.save(documentType);


        Assertions.assertThat(documentTypeRepository.findByName("договор").get())
                .matches(list -> list.getId().equals(documentType.getId()))
                .matches(list -> list.getName().equals(documentType.getName()))
                .matches(list -> list.getDescription().equals(documentType.getDescription()))
                .matches(list -> list.getAttributes().contains(attribute));
    }
}