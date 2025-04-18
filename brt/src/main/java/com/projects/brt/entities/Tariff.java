package com.projects.brt.entities;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "tariffs")
@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tariff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "tariff")
    private User user;

    @NonNull
    private String name;
}
