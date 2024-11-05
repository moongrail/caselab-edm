package ru.caselab.edm.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
    @JoinColumn(name = "approvement_item_id", referencedColumnName = "id")
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
