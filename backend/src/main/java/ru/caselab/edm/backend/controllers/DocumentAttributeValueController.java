package ru.caselab.edm.backend.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.caselab.edm.backend.dto.DocumentAttributeValueCreateDTO;
import ru.caselab.edm.backend.dto.DocumentAttributeValueUpdateDTO;
import ru.caselab.edm.backend.dto.DocumentAttributeValueDTO;
import ru.caselab.edm.backend.service.DocumentAttributeValueService;

@RestController
@RequestMapping("/attribute-values")
@SecurityRequirement(name = "bearer-jwt")
public class DocumentAttributeValueController {

    private final DocumentAttributeValueService documentAttributeValueService;


    public DocumentAttributeValueController(DocumentAttributeValueService attributeValueService) {
        this.documentAttributeValueService = attributeValueService;
    }

    @PostMapping
    public ResponseEntity<DocumentAttributeValueDTO> createAttribute(@Valid @RequestBody DocumentAttributeValueCreateDTO valueDTO) {

        DocumentAttributeValueDTO createValue = documentAttributeValueService.createDocumentAttributeValue(valueDTO);
        return new ResponseEntity<>(createValue, HttpStatus.CREATED);
    }

    @GetMapping("/{documentId}/{attributeId}")
    public ResponseEntity<DocumentAttributeValueDTO> getAttributeValueById(@PathVariable Long documentId, @PathVariable Long attributeId) {
        DocumentAttributeValueDTO value = documentAttributeValueService.getDocumentAttributeValueByDocumentAndAttribute(documentId, attributeId);
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentAttributeValueDTO> getAttributeValueById(@PathVariable Long id) {
        DocumentAttributeValueDTO value = documentAttributeValueService.getDocumentAttributeValueById(id);
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentAttributeValueDTO> updateAttribute(
            @PathVariable Long id,
            @RequestBody DocumentAttributeValueUpdateDTO updateValue) {
        DocumentAttributeValueDTO value = documentAttributeValueService.updateDocumentAttributeValue(id, updateValue);
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttribute(@PathVariable Long id) {
        documentAttributeValueService.deleteAttributeValue(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
