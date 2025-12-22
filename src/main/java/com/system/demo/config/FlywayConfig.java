package com.system.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    @DependsOn("dataSource")
    public Flyway flyway(DataSource dataSource) {
        log.info("Initializing Flyway with datasource: {}", dataSource);
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();

        log.info("Flyway configured successfully. Migration will run on bean initialization.");
        return flyway;
    }
}
