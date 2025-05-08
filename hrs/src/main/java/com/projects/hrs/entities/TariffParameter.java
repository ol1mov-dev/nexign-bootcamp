package com.projects.hrs.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tariff_parameters")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TariffParameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "payment_period_in_days")
    private int paymentPeriodInDays;

    @OneToOne
    @JoinColumn(name = "limit_id", referencedColumnName = "id")
    private Limit limit;

    @OneToOne(mappedBy = "tariffParameters")
    private Tariff tariff;
}
