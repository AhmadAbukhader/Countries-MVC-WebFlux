package com.system.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountryDto {
    private UUID id;

    @NotBlank(message = "Name is required")
    private String name;

    private String officialName;
    private String capital;
    private String region;
    private String subregion;

    @NotNull(message = "Population is required")
    @Positive(message = "Population must be positive")
    private Long population;

    private BigDecimal area;
    private String currencyCode;
    private String currencyName;
}
