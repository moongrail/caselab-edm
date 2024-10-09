package ru.caselab.edm.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.w3c.dom.DocumentType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="attributes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
    @Column(nullable = false)
    private String dataType;
    @ManyToMany(mappedBy = "doc_type_id")
    private List<DocumentType> documentTypes = new ArrayList<>();
}
