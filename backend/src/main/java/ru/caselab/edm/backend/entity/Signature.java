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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "hash", nullable = false, columnDefinition = "TEXT")
    private String hash;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Signature other)) {
            return false;
        }
        return getId() != null && Objects.equals(getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
