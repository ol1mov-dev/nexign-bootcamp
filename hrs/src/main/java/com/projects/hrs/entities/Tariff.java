package com.projects.hrs.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tariffs")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tariff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "parameters_id", nullable = false)
    private Long parametersId;

    @OneToOne(mappedBy = "tariff")
    private Client client;
}