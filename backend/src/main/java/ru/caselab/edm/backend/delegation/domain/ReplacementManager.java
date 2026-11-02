package ru.caselab.edm.backend.delegation.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

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

    @Column(name = "manager_user_id", nullable = false)
    private UUID managerUserId;

    @Column(name = "temp_manager_user_id", nullable = false)
    private UUID tempManagerUserId;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;
}
