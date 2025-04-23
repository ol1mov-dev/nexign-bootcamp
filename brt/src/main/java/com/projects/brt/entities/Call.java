package com.projects.brt.entities;

import com.projects.brt.commons.CallType;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "calls")
@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Call {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NonNull
    private String strangerMsisdn;

    @NonNull
    private String callType;

    @NonNull
    private String startTime;

    @NonNull
    private String endTime;

    @NonNull
    private String duration;
}
