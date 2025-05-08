package com.projects.hrs.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "limits")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Limit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "minutes_for_outcoming", nullable = false)
    private int minutesForOutcome;

    @Column(name = "minutes_for_incoming", nullable = false)
    private int minutesForIncome;

    @Column(name = "price_per_additional_minute_outcoming", nullable = false)
    private BigDecimal pricePerAdditionalMinuteOutcoming;

    @Column(name = "price_per_additional_minute_incoming", nullable = false)
    private BigDecimal pricePerAdditionalMinuteIncoming;

    @OneToOne(mappedBy = "limit")
    private TariffParameter tariffParameter;
}
