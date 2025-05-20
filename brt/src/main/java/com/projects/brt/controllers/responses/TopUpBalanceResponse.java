package com.projects.brt.controllers.responses;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record TopUpBalanceResponse(
        Long abonentId,
        BigDecimal totalBalance
){}
