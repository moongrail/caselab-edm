package ru.caselab.edm.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "signatures")
public class Signature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "approvement item_id", referencedColumnName = "id")
    private ApprovementProcessItem approvementProcessItem;

/*    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;*/

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "hash", nullable = false, columnDefinition = "TEXT")
    private String hash;

/*    @ManyToOne
    @JoinColumn(name = "document_version_id", nullable = false)
    private DocumentVersion documentVersion;*/
}
