package ru.caselab.edm.backend.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.caselab.edm.backend.dto.DocumentTypeCreateDTO;
import ru.caselab.edm.backend.dto.DocumentTypeDTO;
import ru.caselab.edm.backend.dto.DocumentTypeUpdateDTO;
import ru.caselab.edm.backend.dto.DocumentsAttributesDTO;
import ru.caselab.edm.backend.entity.DocumentAttribute;
import ru.caselab.edm.backend.entity.DocumentType;
import ru.caselab.edm.backend.service.DocumentTypeService;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DocumentTypeController.class)
class DocumentTypeControllerTest {
    @MockBean
    private DocumentTypeService documentTypeService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createDocumentType() throws Exception {
        //Входные данные
        List<Long> attributesDocumentTypeId = new ArrayList<>(List.of(0L));

        DocumentTypeCreateDTO testDto = new DocumentTypeCreateDTO();
        testDto.setName("договор");
        testDto.setDescription("какое-то описание");
        testDto.setAttributesDocumentTypeId(attributesDocumentTypeId);

        //выходные данные
        DocumentsAttributesDTO testDocumentsAttributesDTO = getDocumentsAttributesDTO(0L,
                "подписант");

        List<DocumentsAttributesDTO> documentsAttributesDTOList = new ArrayList<>();
        documentsAttributesDTOList.add(testDocumentsAttributesDTO);

        DocumentTypeDTO testDocumentTypeDTO = getDocumentTypeDTO(documentsAttributesDTOList,
                1L, "договор",
                "какое-то описание");

        Mockito.when(documentTypeService.createDocumentType(testDto)).thenReturn(testDocumentTypeDTO);

        mockMvc.perform(post("/document_type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""                            
                                {
                                    "name": "договор",
                                    "description": "какое-то описание",
                                    "attributesDocumentTypeId": [
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
                                    "createAt": "2024-02-22T09:49:19",
                                    "attributes": [
                                                    {
                                                        "id": 0,
                                                        "name": "подписант"
                                                     }
                                                  ]
                                }
                        """))
                .andExpect(status().isCreated());
    }

    @Test
    void showAllDocumentTypes() throws Exception {
        //генерация данных
        DocumentsAttributesDTO testDocumentsAttributesDTO1 = getDocumentsAttributesDTO(0L, "подписант");
        DocumentsAttributesDTO testDocumentsAttributesDTO2 = getDocumentsAttributesDTO(1L, "Ответственный исполнитель");


        List<DocumentsAttributesDTO> documentsAttributesDTOList1 = new ArrayList<>();
        documentsAttributesDTOList1.add(testDocumentsAttributesDTO1);

        List<DocumentsAttributesDTO> documentsAttributesDTOList2 = new ArrayList<>();
        documentsAttributesDTOList2.add(testDocumentsAttributesDTO1);
        documentsAttributesDTOList2.add(testDocumentsAttributesDTO2);

        DocumentTypeDTO testDocumentTypeDTO1 = getDocumentTypeDTO(documentsAttributesDTOList1,
                0L, "договор",
                "какое-то описание");

        DocumentTypeDTO testDocumentTypeDTO2 = getDocumentTypeDTO(documentsAttributesDTOList2,
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
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "content":[
                                {
                                    "id":0,
                                    "name":"договор",
                                    "description":"какое-то описание",
                                    "createAt":"2024-02-22T09:49:19",
                                    "attributes":
                                        [
                                            {
                                                "id":0,
                                                "name":"подписант"
                                            }
                                        ]
                                },
                                {
                                    "id":1,
                                    "name":"котировка",
                                    "description":"какое-то описание котировки",
                                    "createAt":"2024-02-22T09:49:19",
                                    "attributes":
                                        [
                                            {
                                                "id":0,
                                                "name":"подписант"
                                            },
                                            {
                                                "id":1,
                                                "name":"Ответственный исполнитель"
                                            }
                                        ]
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
                        }
                        """));
    }

    @Test
    void showDocumentType() throws Exception {
        //генерация данных
        LocalDateTime now = LocalDateTime
                .of(2024, Month.FEBRUARY, 22, 9, 49, 19);
        DocumentsAttributesDTO testDocumentsAttributesDTO1 = getDocumentsAttributesDTO(0L, "подписант");
        DocumentsAttributesDTO testDocumentsAttributesDTO2 = getDocumentsAttributesDTO(1L, "Ответственный исполнитель");

        List<DocumentsAttributesDTO> documentsAttributesDTOList1 = new ArrayList<>();
        documentsAttributesDTOList1.add(testDocumentsAttributesDTO1);

        List<DocumentsAttributesDTO> documentsAttributesDTOList2 = new ArrayList<>();
        documentsAttributesDTOList2.add(testDocumentsAttributesDTO1);
        documentsAttributesDTOList2.add(testDocumentsAttributesDTO2);

        DocumentTypeDTO testDocumentTypeDTO1 = getDocumentTypeDTO(documentsAttributesDTOList1,
                0L, "договор",
                "какое-то описание");

        DocumentTypeDTO testDocumentTypeDTO2 = getDocumentTypeDTO(documentsAttributesDTOList2,
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
                                    "createAt":"2024-02-22T09:49:19",
                                    "attributes":
                                        [
                                            {
                                                "id":0,
                                                "name":"подписант"
                                            },
                                            {
                                                "id":1,
                                                "name":"Ответственный исполнитель"
                                            }
                                        ]
                        }
                        """));
    }

    @Test
    void updateDocumentType() throws Exception {
        DocumentTypeUpdateDTO documentTypeUpdateDTO = new DocumentTypeUpdateDTO();
        documentTypeUpdateDTO.setAttributesDocumentTypeId(List.of(1L));
        documentTypeUpdateDTO.setName("Новый документ");
        documentTypeUpdateDTO.setDescription("Такого вы еще не видели");

        LocalDateTime now = LocalDateTime
                .of(2024, Month.FEBRUARY, 22, 9, 49, 19, 275039200);

        DocumentType testDocumenttype = new DocumentType();

        DocumentAttribute documentAttribute = new DocumentAttribute();
        documentAttribute.setName("подписант");
        documentAttribute.setDataType("текст");

        List<DocumentAttribute> documentAttributeList = new ArrayList<>();
        documentAttributeList.add(documentAttribute);

        testDocumenttype.setId(1L);
        testDocumenttype.setName("договор");
        testDocumenttype.setDescription("какоей-то описание");
        testDocumenttype.setCreateAt(now);
        testDocumenttype.setAttributes(documentAttributeList);

        Long id = 1L;

        DocumentsAttributesDTO testDocumentsAttributesDTO1 = getDocumentsAttributesDTO(0L, "подписант");

        List<DocumentsAttributesDTO> documentsAttributesDTOList1 = new ArrayList<>();
        documentsAttributesDTOList1.add(testDocumentsAttributesDTO1);

        DocumentTypeDTO expected = getDocumentTypeDTO(documentsAttributesDTOList1,
                0L, "Новый документ",
                "Такого вы еще не видели");

        Mockito.when(documentTypeService.updateDocumentType(id, documentTypeUpdateDTO))
                .thenReturn(expected);

        mockMvc.perform(patch("/document_type/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""                            
                                {
                                    "name":"Новый документ",
                                    "description":"Такого вы еще не видели",
                                    "attributesDocumentTypeId": [
                                                     1
                                                  ]
                                }
                                """
                        ))
                .andExpect(content().json("""
                        {
                                    "id": 0,
                                    "name": "Новый документ",
                                    "description": "Такого вы еще не видели",
                                    "createAt": "2024-02-22T09:49:19",
                                    "attributes": [
                                                    {
                                                        "id": 0,
                                                        "name": "подписант"
                                                     }
                                                  ]
                                }
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void deleteDocumentType() throws Exception {
        mockMvc.perform(delete("/document_type/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Mockito.verify(documentTypeService).deleteDocumentType(1L);
    }

    private static DocumentsAttributesDTO getDocumentsAttributesDTO(Long attributesId,
                                                                    String attributesName) {
        DocumentsAttributesDTO testDocumentsAttributesDTO = new DocumentsAttributesDTO();
        testDocumentsAttributesDTO.setId(attributesId);
        testDocumentsAttributesDTO.setName(attributesName);
        return testDocumentsAttributesDTO;
    }

    private static DocumentTypeDTO getDocumentTypeDTO(List<DocumentsAttributesDTO> documentsAttributesDTOList,
                                                      Long documentTypeId,
                                                      String nameDocumentType,
                                                      String descriptionDocumentType) {
        LocalDateTime now = LocalDateTime
                .of(2024, Month.FEBRUARY, 22, 9, 49, 19);

        DocumentTypeDTO testDocumentTypeDTO = new DocumentTypeDTO();
        testDocumentTypeDTO.setId(documentTypeId);
        testDocumentTypeDTO.setName(nameDocumentType);
        testDocumentTypeDTO.setDescription(descriptionDocumentType);
        testDocumentTypeDTO.setCreateAt(now);
        testDocumentTypeDTO.setAttributes(documentsAttributesDTOList);
        return testDocumentTypeDTO;
    }
}