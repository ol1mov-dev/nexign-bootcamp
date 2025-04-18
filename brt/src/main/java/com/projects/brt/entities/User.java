package com.projects.brt.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Table(name = "users")
@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String firstName;

    @NonNull
    private String name;

    @Nullable
    private String lastName;

    @NonNull
    private String msisdn;

    @OneToMany(mappedBy = "user")
    private Set<Call> calls;

    @NonNull
    private String balance;

    @OneToOne
    @JoinColumn(name = "tariff_id")
    private Tariff tariff;
}
