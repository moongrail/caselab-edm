package ru.caselab.edm.backend.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import ru.caselab.edm.backend.dto.attribute.AttributeDTO;
import ru.caselab.edm.backend.dto.documenttype.DocumentTypeCreateDTO;
import ru.caselab.edm.backend.dto.documenttype.DocumentTypeDTO;
import ru.caselab.edm.backend.dto.documenttype.DocumentTypeUpdateDTO;
import ru.caselab.edm.backend.entity.Attribute;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.repository.RoleRepository;
import ru.caselab.edm.backend.repository.UserRepository;
import ru.caselab.edm.backend.repository.elastic.AttributeSearchRepository;
import ru.caselab.edm.backend.security.details.UserDetailsServiceImpl;
import ru.caselab.edm.backend.security.service.impl.JwtServiceImpl;
import ru.caselab.edm.backend.service.DocumentTypeService;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({JwtServiceImpl.class, UserDetailsServiceImpl.class})
@WebMvcTest(controllers = DocumentTypeController.class)
class DocumentTypeControllerTest {
    @MockBean
    AttributeSearchRepository attributeSearchRepository;
    @MockBean
    UserRepository userRepository;
    @MockBean
    RoleRepository roleRepository;
    @MockBean
    private DocumentTypeService documentTypeService;
    @Autowired
    private MockMvc mockMvc;

    private static AttributeDTO getDocumentsAttributesDTO(Long attributesId,
                                                          String attributesName) {
        AttributeDTO testAttributeDTO = new AttributeDTO();
        testAttributeDTO.setId(attributesId);
        testAttributeDTO.setName(attributesName);
        return testAttributeDTO;
    }

    private static DocumentTypeDTO getDocumentTypeDTO(Set<Long> documentsAttributesIdsSet,
                                                      Long documentTypeId,
                                                      String nameDocumentType,
                                                      String descriptionDocumentType) {

        DocumentTypeDTO testDocumentTypeDTO = new DocumentTypeDTO();
        testDocumentTypeDTO.setId(documentTypeId);
        testDocumentTypeDTO.setName(nameDocumentType);
        testDocumentTypeDTO.setDescription(descriptionDocumentType);
        testDocumentTypeDTO.setAttributeIds(documentsAttributesIdsSet);
        return testDocumentTypeDTO;
    }

    @Test
    @WithMockUser
    void createDocumentType() throws Exception {
        //Входные данные
        Set<Long> attributesDocumentTypeId = new HashSet<>(List.of(0L));

        DocumentTypeCreateDTO testDto = new DocumentTypeCreateDTO();
        testDto.setName("договор");
        testDto.setDescription("какое-то описание");
        testDto.setAttributeIds(attributesDocumentTypeId);

        //выходные данные
        AttributeDTO testAttributeDto = getDocumentsAttributesDTO(0L,
                "подписант");

        Set<Long> documentsAttributesDTOList = new HashSet<>();
        documentsAttributesDTOList.add(testAttributeDto.getId());

        DocumentTypeDTO testDocumentTypeDTO = getDocumentTypeDTO(documentsAttributesDTOList,
                1L, "договор",
                "какое-то описание");

        Mockito.when(documentTypeService.createDocumentType(testDto)).thenReturn(testDocumentTypeDTO);

        mockMvc.perform(post("/document_type")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "договор",
                                    "description": "какое-то описание",
                                    "attributeIds": [
                                                     0
                                                  ]
                                }
                                """
                        ))
                .andExpect(content().json("""
                        {
                                    "id": 1,
                                    "name": "договор",
                                    "description": "какое-то описание",
                                    "attributeIds": [
                                                   0
                                                  ]
                                }
                        """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void showAllDocumentTypes() throws Exception {
        //генерация данных
        AttributeDTO testAttributeDto1 = getDocumentsAttributesDTO(0L, "подписант");
        AttributeDTO testAttributeDto2 = getDocumentsAttributesDTO(1L, "Ответственный исполнитель");


        Set<Long> documentsAttributesDTOSet1 = new HashSet<>();
        documentsAttributesDTOSet1.add(testAttributeDto1.getId());

        Set<Long> documentsAttributesDTOSet2 = new HashSet<>();
        documentsAttributesDTOSet2.add(testAttributeDto1.getId());
        documentsAttributesDTOSet2.add(testAttributeDto2.getId());

        DocumentTypeDTO testDocumentTypeDTO1 = getDocumentTypeDTO(documentsAttributesDTOSet1,
                0L, "договор",
                "какое-то описание");

        DocumentTypeDTO testDocumentTypeDTO2 = getDocumentTypeDTO(documentsAttributesDTOSet2,
                1L, "котировка",
                "какое-то описание котировки");

        List<DocumentTypeDTO> list = new ArrayList<>();
        list.add(testDocumentTypeDTO1);
        list.add(testDocumentTypeDTO2);

        int page = 0;
        int size = 2;
        Page<DocumentTypeDTO> expected = new PageImpl<>(list, PageRequest.of(page, size), 2);

        Mockito.when(documentTypeService.getAllDocumentType(page, size))
                .thenReturn(expected);

        mockMvc.perform(get("/document_type?page=0&size=2")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "content":[
                                {
                                    "id":0,
                                    "name":"договор",
                                    "description":"какое-то описание",
                                    "attributeIds":
                                        [0
                                        ]
                                },
                                {
                                    "id":1,
                                    "name":"котировка",
                                    "description":"какое-то описание котировки",
                               "attributeIds":
                                        [0,1]
                        
                                }
                                        ],
                                        "pageable":
                                            {
                                                "pageNumber":0,
                                                "pageSize":2,
                                                "sort":
                                                    {
                                                        "empty":true,
                                                        "sorted":false,
                                                        "unsorted":true
                                                    },
                                                "offset":0,
                                                "paged":true,
                                                "unpaged":false
                                            },
                                        "last":true,
                                        "totalPages":1,
                                        "totalElements":2,
                                        "first":true,
                                        "size":2,
                                        "number":0,
                                        "sort":
                                            {
                                                "empty":true,
                                                "sorted":false,
                                                "unsorted":true
                                            },
                                        "numberOfElements":2,
                                        "empty":false
                        }
                        """));
    }

    @Test
    @WithMockUser
    void showDocumentType() throws Exception {
        //генерация данных
        LocalDateTime now = LocalDateTime
                .of(2024, Month.FEBRUARY, 22, 9, 49, 19);
        AttributeDTO testAttributeDto1 = getDocumentsAttributesDTO(0L, "подписант");
        AttributeDTO testAttributeDto2 = getDocumentsAttributesDTO(1L, "Ответственный исполнитель");

        Set<Long> documentsAttributesDTOSet1 = new HashSet<>();
        documentsAttributesDTOSet1.add(testAttributeDto1.getId());

        Set<Long> documentsAttributesDTOSet2 = new HashSet<>();
        documentsAttributesDTOSet2.add(testAttributeDto1.getId());
        documentsAttributesDTOSet2.add(testAttributeDto2.getId());

        DocumentTypeDTO testDocumentTypeDTO1 = getDocumentTypeDTO(documentsAttributesDTOSet1,
                0L, "договор",
                "какое-то описание");

        DocumentTypeDTO testDocumentTypeDTO2 = getDocumentTypeDTO(documentsAttributesDTOSet2,
                1L, "котировка",
                "какое-то описание котировки");

        Mockito.when(documentTypeService.getDocumentTypeById(1L))
                .thenReturn(testDocumentTypeDTO2);

        mockMvc.perform(get("/document_type/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "id":1,
                                    "name":"котировка",
                                    "description":"какое-то описание котировки",
                                    "attributeIds":
                                        [
                                            0,1
                                        ]
                        }
                        """));
    }

    @Test
    @WithMockUser
    void updateDocumentType() throws Exception {
        DocumentTypeUpdateDTO documentTypeUpdateDTO = new DocumentTypeUpdateDTO();
        documentTypeUpdateDTO.setAttributeIds(new HashSet<>(List.of(0L)));
        documentTypeUpdateDTO.setName("Новый документ");
        documentTypeUpdateDTO.setDescription("Такого вы еще не видели");


        DocumentType testDocumenttype = new DocumentType();

        Attribute attribute = new Attribute();
        attribute.setName("подписант");
        attribute.setDataType("текст");

        Set<Attribute> documentAttributeList = new HashSet<>();
        documentAttributeList.add(attribute);

        testDocumenttype.setId(1L);
        testDocumenttype.setName("договор");
        testDocumenttype.setDescription("какоей-то описание");

        Long id = 1L;

        AttributeDTO testAttributeDto1 = getDocumentsAttributesDTO(0L, "подписант");

        Set<Long> documentsAttributesDTOSet1 = new HashSet<>();
        documentsAttributesDTOSet1.add(testAttributeDto1.getId());

        DocumentTypeDTO expected = getDocumentTypeDTO(documentsAttributesDTOSet1,
                1L, "Новый документ",
                "Такого вы еще не видели");

        Mockito.when(documentTypeService.updateDocumentType(id, documentTypeUpdateDTO))
                .thenReturn(expected);

        mockMvc.perform(patch("/document_type/{id}", 1)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name":"Новый документ",
                                    "description":"Такого вы еще не видели",
                                    "attributeIds": [
                                                     0
                                                  ]
                                }
                                """
                        ))
                .andExpect(content().json("""
                        {
                                    "id": 1,
                                    "name": "Новый документ",
                                    "description": "Такого вы еще не видели",
                                    "attributeIds": [
                                                  0
                                                  ]
                                }
                        """))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deleteDocumentType() throws Exception {
        mockMvc.perform(delete("/document_type/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Mockito.verify(documentTypeService).deleteDocumentType(1L);
    }
}