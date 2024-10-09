package ru.caselab.edm.backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.caselab.edm.backend.dto.AttributeDTO;
import ru.caselab.edm.backend.dto.AttributeUpdateDTO;
import ru.caselab.edm.backend.service.AttributeService;

import java.util.List;

@RestController
@RequestMapping("/attributes")
public class AttributeController {
    private final AttributeService attributeService;

    public AttributeController(AttributeService attributeService) {
        this.attributeService = attributeService;
    }



    @GetMapping("/{id}")
    public ResponseEntity<AttributeDTO> getAttributeById(@PathVariable Long id) {
        AttributeDTO attributeDTO = attributeService.getAttributeById(id);
        return new ResponseEntity<>(attributeDTO, HttpStatus.OK);
    }


    @GetMapping
    public ResponseEntity<List<AttributeDTO>> getAllAttributes() {
        List<AttributeDTO> attributes = attributeService.getAllAttributes();
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
