package com.system.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestCountriesCountryResponse {

    @JsonProperty("name")
    private RestCountriesNameInfo name;

    @JsonProperty("capital")
    private List<String> capital;

    @JsonProperty("region")
    private String region;

    @JsonProperty("subregion")
    private String subregion;

    @JsonProperty("population")
    private Long population;

    @JsonProperty("area")
    private Double area;

    @JsonProperty("currencies")
    private Map<String, RestCountriesCurrencyInfo> currencies;
}
