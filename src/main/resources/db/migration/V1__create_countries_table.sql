-- Set search path to the schema for this session
SET search_path TO countries_mvc_schema;

CREATE TABLE countries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),   -- Replace with uuid_generate_v7() if supported

    country_name        VARCHAR(150) NOT NULL,
    official_name       VARCHAR(200),

    capital_city        VARCHAR(150),

    region              VARCHAR(100),
    subregion           VARCHAR(100),

    population          BIGINT CHECK (population >= 0),
    area                DECIMAL(15, 2) CHECK (area >= 0),

    currency_code       CHAR(3),
    currency_name       VARCHAR(120),

    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


