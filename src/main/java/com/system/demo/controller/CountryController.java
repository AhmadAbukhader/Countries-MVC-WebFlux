package com.system.demo.controller;

import com.system.demo.dto.CountryDto;
import com.system.demo.dto.PaginatedResponse;
import com.system.demo.service.CountryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncCountries() {
        log.info("Received request to sync countries from external API");
        int count = countryService.syncCountriesFromExternalApi();
        log.info("Sync completed, {} countries synchronized", count);
        return ResponseEntity.ok(Map.of(
                "message", "Countries synchronized successfully",
                "count", count));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<CountryDto>> getAllCountries(
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit) {
        log.debug("Received request to get all countries with cursor: {}, limit: {}", cursor, limit);
        PaginatedResponse<CountryDto> response = countryService.getAllCountries(cursor, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CountryDto> getCountryById(@PathVariable UUID id) {
        log.debug("Received request to get country by id: {}", id);
        CountryDto country = countryService.getCountryById(id);
        return ResponseEntity.ok(country);
    }

    @PostMapping
    public ResponseEntity<CountryDto> createCountry(@Valid @RequestBody CountryDto countryDto) {
        log.info("Received request to create country: {}", countryDto.getName());
        CountryDto created = countryService.createCountry(countryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CountryDto> updateCountry(
            @PathVariable UUID id,
            @Valid @RequestBody CountryDto countryDto) {
        log.info("Received request to update country with id: {}", id);
        CountryDto updated = countryService.updateCountry(id, countryDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCountry(@PathVariable UUID id) {
        log.info("Received request to delete country with id: {}", id);
        countryService.deleteCountry(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/region/{region}")
    public ResponseEntity<List<CountryDto>> getCountriesByRegion(@PathVariable String region) {
        log.debug("Received request to get countries by region: {}", region);
        List<CountryDto> countries = countryService.getCountriesByRegion(region);
        return ResponseEntity.ok(countries);
    }
}
