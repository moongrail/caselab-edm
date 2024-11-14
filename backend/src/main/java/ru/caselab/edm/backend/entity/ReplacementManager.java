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

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;
}
