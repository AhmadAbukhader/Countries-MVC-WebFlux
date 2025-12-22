package com.system.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestCountriesNameInfo {

    @JsonProperty("common")
    private String common;

    @JsonProperty("official")
    private String official;
}
