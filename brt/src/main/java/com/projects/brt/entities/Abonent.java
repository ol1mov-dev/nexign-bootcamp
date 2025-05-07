package com.projects.brt.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Table(name = "abonents")
@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Abonent{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String firstName;

    @NonNull
    private String name;

    @Nullable
    private String lastName;

    @Column(name = "msisdn", nullable = false, unique = true)
    private String msisdn;

    @OneToMany(mappedBy = "abonent", fetch = FetchType.LAZY)
    private Set<Call> calls;

    @Column(name = "balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal balance;
}
