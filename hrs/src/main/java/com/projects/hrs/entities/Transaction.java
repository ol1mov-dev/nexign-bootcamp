package com.projects.hrs.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "transactions")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @Column(name = "amount_of_minutes", nullable = false)
    private Long amountOfMinutes;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;
}