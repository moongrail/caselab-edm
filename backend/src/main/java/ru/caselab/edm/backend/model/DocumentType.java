package ru.caselab.edm.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "document_type")
@Getter
@Setter
public class DocumentType {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    private String description;
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
    @ManyToMany(mappedBy = attribute_id)
    private List<DocumentAttribute> attributes = new ArrayList<>();
}
