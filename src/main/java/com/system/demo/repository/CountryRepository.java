package com.system.demo.repository;

import com.system.demo.model.Country;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CountryRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_ALL = "SELECT id, country_name, official_name, capital_city, region, subregion, "
            +
            "population, area, currency_code, currency_name, created_at, updated_at " +
            "FROM countries";

    private static final String UPDATE = "UPDATE countries SET country_name = ?, official_name = ?, capital_city = ?, "
            +
            "region = ?, subregion = ?, population = ?, area = ?, currency_code = ?, " +
            "currency_name = ?, updated_at = ? WHERE id = ?";

    private static final String DELETE = "DELETE FROM countries WHERE id = ?";

    private static final String FIND_BY_ID = SELECT_ALL + " WHERE id = ?";

    private static final String FIND_BY_REGION = SELECT_ALL + " WHERE region = ?";

    private static final String FIND_BY_NAME = SELECT_ALL + " WHERE country_name = ?";

    private static final String FIND_BY_ID_GREATER_THAN = SELECT_ALL + " WHERE id > ? ORDER BY id ASC LIMIT ?";

    private static final String FIND_ALL_ORDERED = SELECT_ALL + " ORDER BY id ASC LIMIT ?";

    private static final String EXISTS_BY_ID_GREATER_THAN = "SELECT COUNT(*) > 0 FROM countries WHERE id > ?";

    private final RowMapper<Country> countryRowMapper = new CountryRowMapper();

    public Country save(Country country) {
        // Check if country with the same name already exists
        Optional<Country> existingCountry = findByName(country.getName());

        if (existingCountry.isPresent()) {
            // Update existing country
            Country existing = existingCountry.get();
            log.debug("Updating existing country: {} with id: {}", country.getName(), existing.getId());
            jdbcTemplate.update(UPDATE,
                    country.getName(),
                    country.getOfficialName(),
                    country.getCapital(),
                    country.getRegion(),
                    country.getSubregion(),
                    country.getPopulation(),
                    country.getArea(),
                    country.getCurrencyCode(),
                    country.getCurrencyName(),
                    Timestamp.from(country.getUpdatedAt()),
                    existing.getId());
            // Return updated country with existing ID
            country.setId(existing.getId());
            country.setCreatedAt(existing.getCreatedAt());
            return country;
        } else {
            // Insert new country
            log.debug("Inserting new country: {}", country.getName());
            // Use RETURNING clause to get the generated UUID
            String insertWithReturning = "INSERT INTO countries (country_name, official_name, capital_city, region, subregion, "
                    +
                    "population, area, currency_code, currency_name, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

            UUID generatedId = jdbcTemplate.queryForObject(insertWithReturning, UUID.class,
                    country.getName(),
                    country.getOfficialName(),
                    country.getCapital(),
                    country.getRegion(),
                    country.getSubregion(),
                    country.getPopulation(),
                    country.getArea(),
                    country.getCurrencyCode(),
                    country.getCurrencyName(),
                    Timestamp.from(country.getCreatedAt()),
                    Timestamp.from(country.getUpdatedAt()));

            country.setId(generatedId);
            log.debug("Inserted country with generated id: {}", generatedId);
            return country;
        }
    }

    public Optional<Country> findById(UUID id) {
        log.debug("Finding country by id: {}", id);
        List<Country> countries = jdbcTemplate.query(FIND_BY_ID, countryRowMapper, id);
        return countries.isEmpty() ? Optional.empty() : Optional.of(countries.get(0));
    }

    public List<Country> findByRegion(String region) {
        log.debug("Finding countries by region: {}", region);
        return jdbcTemplate.query(FIND_BY_REGION, countryRowMapper, region);
    }

    public Optional<Country> findByName(String name) {
        log.debug("Finding country by name: {}", name);
        List<Country> countries = jdbcTemplate.query(FIND_BY_NAME, countryRowMapper, name);
        return countries.isEmpty() ? Optional.empty() : Optional.of(countries.get(0));
    }

    public List<Country> findByIdGreaterThanOrderByIdAsc(UUID cursor, int limit) {
        log.debug("Finding countries with id > {} limit {}", cursor, limit);
        return jdbcTemplate.query(FIND_BY_ID_GREATER_THAN, countryRowMapper, cursor, limit);
    }

    public List<Country> findAllOrderByIdAsc(int limit) {
        log.debug("Finding all countries ordered by id limit {}", limit);
        return jdbcTemplate.query(FIND_ALL_ORDERED, countryRowMapper, limit);
    }

    public boolean existsById(UUID id) {
        log.debug("Checking if country exists with id: {}", id);
        return findById(id).isPresent();
    }

    public void deleteById(UUID id) {
        log.debug("Deleting country with id: {}", id);
        jdbcTemplate.update(DELETE, id);
    }

    public boolean existsByIdGreaterThan(UUID cursor) {
        log.debug("Checking if countries exist with id > {}", cursor);
        Boolean result = jdbcTemplate.queryForObject(EXISTS_BY_ID_GREATER_THAN, Boolean.class, cursor);
        return result != null && result;
    }

    private static class CountryRowMapper implements RowMapper<Country> {
        @Override
        public Country mapRow(ResultSet rs, int rowNum) throws SQLException {
            Country country = new Country();
            country.setId((UUID) rs.getObject("id"));
            country.setName(rs.getString("country_name"));
            country.setOfficialName(rs.getString("official_name"));
            country.setCapital(rs.getString("capital_city"));
            country.setRegion(rs.getString("region"));
            country.setSubregion(rs.getString("subregion"));

            long population = rs.getLong("population");
            country.setPopulation(rs.wasNull() ? null : population);

            BigDecimal area = rs.getBigDecimal("area");
            country.setArea(area);

            country.setCurrencyCode(rs.getString("currency_code"));
            country.setCurrencyName(rs.getString("currency_name"));

            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                country.setCreatedAt(createdAt.toInstant());
            }

            Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) {
                country.setUpdatedAt(updatedAt.toInstant());
            }

            return country;
        }
    }
}
