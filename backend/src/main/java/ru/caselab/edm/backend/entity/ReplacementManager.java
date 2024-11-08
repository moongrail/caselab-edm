package ru.caselab.edm.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "replacement_managers")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReplacementManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "manager_user_id", nullable = false)
    private User managerUserId;

    @ManyToOne
    @JoinColumn(name = "temp_manager_user_id", nullable = false)
    private User tempManagerUserId;

    @Column(name = "from", nullable = false)
    private Instant from;

    @Column(name = "to", nullable = false)
    private Instant to;
}
