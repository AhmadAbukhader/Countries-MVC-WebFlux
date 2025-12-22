package com.system.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Country {
    private UUID id;
    private String name;
    private String officialName;
    private String capital;
    private String region;
    private String subregion;
    private Long population;
    private BigDecimal area;
    private String currencyCode;
    private String currencyName;
    private Instant createdAt;
    private Instant updatedAt;
}
