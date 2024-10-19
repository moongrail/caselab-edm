package ru.caselab.edm.backend.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.caselab.edm.backend.dto.AttributeCreateDTO;
import ru.caselab.edm.backend.dto.AttributeDTO;
import ru.caselab.edm.backend.dto.AttributeUpdateDTO;
import ru.caselab.edm.backend.service.AttributeService;

@RestController
@RequestMapping("/attributes")
@SecurityRequirement(name = "bearer-jwt")
public class AttributeController {
    private final AttributeService attributeService;

    public AttributeController(AttributeService attributeService) {
        this.attributeService = attributeService;
    }


    @PostMapping
    public ResponseEntity<AttributeDTO> createAttribute(@Valid @RequestBody AttributeCreateDTO attributeDTO) {

        AttributeDTO attribute = attributeService.createAttribute(attributeDTO);
        return new ResponseEntity<>(attribute, HttpStatus.CREATED);
    }


    @GetMapping("/{id}")
    public ResponseEntity<AttributeDTO> getAttributeById(@PathVariable Long id) {
        AttributeDTO attributeDTO = attributeService.getAttributeById(id);
        return new ResponseEntity<>(attributeDTO, HttpStatus.OK);
    }


    @GetMapping
    public ResponseEntity<Page<AttributeDTO>> getAllAttributes(@RequestParam(value = "page", defaultValue = "0") int page,
                                                               @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<AttributeDTO> attributes = attributeService.getAllAttributes(page, size);
        return new ResponseEntity<>(attributes, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttributeDTO> updateAttribute(
            @PathVariable Long id,
            @RequestBody AttributeUpdateDTO updateAttributeDTO) {
        AttributeDTO updatedAttribute = attributeService.updateAttribute(id, updateAttributeDTO);
        return new ResponseEntity<>(updatedAttribute, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttribute(@PathVariable Long id) {
        attributeService.deleteAttribute(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
