package com.system.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestCountriesCurrencyInfo {

    @JsonProperty("name")
    private String name;

    @JsonProperty("symbol")
    private String symbol;
}
