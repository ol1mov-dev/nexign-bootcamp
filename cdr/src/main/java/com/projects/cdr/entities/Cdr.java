package com.projects.cdr.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "cdrs")
@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cdr {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String callType;

    @NonNull
    private String msisdn1;

    @NonNull
    private String msisdn2;

    @NonNull
    private String startTime;

    @NonNull
    private String endTime;
}
