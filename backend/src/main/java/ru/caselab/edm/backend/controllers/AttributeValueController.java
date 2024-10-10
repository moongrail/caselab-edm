package ru.caselab.edm.backend.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.caselab.edm.backend.dto.*;
import ru.caselab.edm.backend.service.AttributeValueService;

@RestController
@RequestMapping("/attribute-values")
public class AttributeValueController {

    private final AttributeValueService attributeValueService;


    public AttributeValueController(AttributeValueService attributeValueService) {
        this.attributeValueService = attributeValueService;
    }
    @PostMapping
    public ResponseEntity<AttributeValueDTO> createAttribute(@Valid @RequestBody AttributeValueCreateDTO valueDTO) {

        AttributeValueDTO createValue=attributeValueService.createAttributeValue(valueDTO);
        return new ResponseEntity<>(createValue, HttpStatus.CREATED);
    }

    @GetMapping("/{documentId}/{attributeId}")
    public ResponseEntity<AttributeValueDTO> getAttributeValueById(@PathVariable Long documentId,@PathVariable Long attributeId ) {
        AttributeValueDTO value = attributeValueService.getAttributeValueByDocumentAndAttribute(documentId,attributeId);
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttributeValueDTO> getAttributeValueById(@PathVariable Long id) {
        AttributeValueDTO value = attributeValueService.getAttributeValueById(id);
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttributeValueDTO> updateAttribute(
            @PathVariable Long id,
            @RequestBody AttributeValueUpdateDTO updateValue) {
        AttributeValueDTO value = attributeValueService.updateAttributeValue(id,updateValue);
        return new ResponseEntity<>(value, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttribute(@PathVariable Long id) {
        attributeValueService.deleteAttributeValue(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
