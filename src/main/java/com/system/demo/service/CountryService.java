package com.system.demo.service;

import com.system.demo.dto.CountryDto;
import com.system.demo.dto.PaginatedResponse;
import com.system.demo.integration.RestCountriesApiClient;
import com.system.demo.model.Country;
import com.system.demo.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;
    private final RestCountriesApiClient restCountriesApiClient;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final long MIN_POPULATION = 1_000_000L;

    @Transactional
    public int syncCountriesFromExternalApi() {
        log.info("Starting synchronization of countries from external API");

        Instant now = Instant.now();
        Scheduler fetchScheduler = Schedulers.newSingle("fetching-countries");
        Scheduler saveScheduler = Schedulers.newBoundedElastic(150, 10, "saveCountries");

        Integer savedCount = restCountriesApiClient.fetchAllCountries()
                .publishOn(fetchScheduler)
                .filter(country -> {
                    boolean passes = country.getPopulation() != null && country.getPopulation() > MIN_POPULATION;
                    if (!passes) {
                        log.debug("Filtered out country with population: {}", country.getPopulation());
                    }
                    return passes;
                })
                .map(country -> {
                    country.setId(null);
                    country.setCreatedAt(now);
                    country.setUpdatedAt(now);
                    return country;
                })
                .collectList()
                .doOnSuccess(list -> log.info("Fetched {} countries from external API", list.size()))
                .publishOn(saveScheduler)
                .flatMap(countries -> {
                    if (countries == null || countries.isEmpty()) {
                        log.warn("No countries retrieved from external API");
                        return reactor.core.publisher.Mono.just(0);
                    }

                    // Delete all existing countries and batch insert new ones
                    log.info("Deleting all existing countries before batch insert");
                    countryRepository.deleteAll();

                    int count = countryRepository.saveBatch(countries);
                    log.info("Successfully synchronized {} countries", count);

                    return reactor.core.publisher.Mono.just(count);
                })
                .doOnError(error -> log.error("Error during synchronization", error))
                .doFinally(signalType -> {
                    fetchScheduler.dispose();
                    saveScheduler.dispose();
                })
                .block();

        return savedCount != null ? savedCount : 0;
    }

    public PaginatedResponse<CountryDto> getAllCountries(String cursor, Integer limit) {
        log.debug("Getting all countries with cursor: {}, limit: {}", cursor, limit);
        int pageSize = (limit != null && limit > 0) ? Math.min(limit, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;

        List<Country> countries;
        UUID cursorUuid = null;

        if (cursor != null && !cursor.isEmpty()) {
            try {
                cursorUuid = UUID.fromString(cursor);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid cursor format: {}, treating as first page", cursor);
                cursorUuid = null;
            }
        }

        if (cursorUuid != null) {
            countries = countryRepository.findByIdGreaterThanOrderByIdAsc(cursorUuid, pageSize + 1);
        } else {
            countries = countryRepository.findAllOrderByIdAsc(pageSize + 1);
        }

        boolean hasMore = countries.size() > pageSize;
        if (hasMore) {
            countries = countries.subList(0, pageSize);
        }

        String nextCursor = null;
        if (hasMore && !countries.isEmpty()) {
            nextCursor = countries.get(countries.size() - 1).getId().toString();
        }

        List<CountryDto> countryDtos = countries.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        log.debug("Returning {} countries, hasMore: {}", countryDtos.size(), hasMore);
        return new PaginatedResponse<>(countryDtos, nextCursor, hasMore);
    }

    public CountryDto getCountryById(UUID id) {
        log.debug("Getting country by id: {}", id);
        return countryRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> {
                    log.error("Country not found with id: {}", id);
                    return new RuntimeException("Country not found with id: " + id);
                });
    }

    @Transactional
    public CountryDto createCountry(CountryDto countryDto) {
        log.info("Creating new country: {}", countryDto.getName());
        Country country = mapToEntity(countryDto);
        country.setId(null); // Let database generate the UUID
        Instant now = Instant.now();
        country.setCreatedAt(now);
        country.setUpdatedAt(now);
        Country saved = countryRepository.save(country);
        log.info("Successfully created country with id: {}", saved.getId());
        return mapToDto(saved);
    }

    @Transactional
    public CountryDto updateCountry(UUID id, CountryDto countryDto) {
        log.info("Updating country with id: {}", id);
        Country existing = countryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Country not found with id: {}", id);
                    return new RuntimeException("Country not found with id: " + id);
                });

        existing.setName(countryDto.getName());
        existing.setOfficialName(countryDto.getOfficialName());
        existing.setCapital(countryDto.getCapital());
        existing.setRegion(countryDto.getRegion());
        existing.setSubregion(countryDto.getSubregion());
        existing.setPopulation(countryDto.getPopulation());
        existing.setArea(countryDto.getArea());
        existing.setCurrencyCode(countryDto.getCurrencyCode());
        existing.setCurrencyName(countryDto.getCurrencyName());
        existing.setUpdatedAt(Instant.now());

        Country updated = countryRepository.save(existing);
        log.info("Successfully updated country with id: {}", id);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteCountry(UUID id) {
        log.info("Deleting country with id: {}", id);
        if (!countryRepository.existsById(id)) {
            log.error("Country not found with id: {}", id);
            throw new RuntimeException("Country not found with id: " + id);
        }
        countryRepository.deleteById(id);
        log.info("Successfully deleted country with id: {}", id);
    }

    public List<CountryDto> getCountriesByRegion(String region) {
        log.debug("Getting countries by region: {}", region);
        List<CountryDto> countries = countryRepository.findByRegion(region).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        log.debug("Found {} countries in region: {}", countries.size(), region);
        return countries;
    }

    private CountryDto mapToDto(Country country) {
        return new CountryDto(
                country.getId(),
                country.getName(),
                country.getOfficialName(),
                country.getCapital(),
                country.getRegion(),
                country.getSubregion(),
                country.getPopulation(),
                country.getArea(),
                country.getCurrencyCode(),
                country.getCurrencyName());
    }

    private Country mapToEntity(CountryDto dto) {
        Country country = new Country();
        country.setId(dto.getId());
        country.setName(dto.getName());
        country.setOfficialName(dto.getOfficialName());
        country.setCapital(dto.getCapital());
        country.setRegion(dto.getRegion());
        country.setSubregion(dto.getSubregion());
        country.setPopulation(dto.getPopulation());
        country.setArea(dto.getArea());
        country.setCurrencyCode(dto.getCurrencyCode());
        country.setCurrencyName(dto.getCurrencyName());
        return country;
    }
}
