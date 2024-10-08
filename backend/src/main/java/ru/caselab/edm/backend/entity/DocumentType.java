package ru.caselab.edm.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "document_types")
@Data
public class DocumentType {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    private String description;
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
    @ManyToMany
    @JoinTable(
            name = "document_type_attributes",
            joinColumns = @JoinColumn(name = "doc_type_id "),
            inverseJoinColumns = @JoinColumn(name = "attribute_id")
    )
    private List<DocumentAttribute> attributes = new ArrayList<>();
}
