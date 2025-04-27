package com.projects.hrs.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tariff_parameters")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TariffParameters {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "monthly_price", nullable = false)
    private Long monthlyPrice;

    @Column(name = "monthly_amount_of_minutes", nullable = false)
    private Long monthlyAmountOfMinutes;
}
