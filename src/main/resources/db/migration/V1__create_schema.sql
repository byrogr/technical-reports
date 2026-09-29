-- Esquema inicial del sistema de informes técnicos (ver DESIGN.md, sección 5)

CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    email         VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_email_lowercase CHECK (email = LOWER(email))
);

CREATE TABLE clients (
    id              BIGSERIAL    PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    document_type   VARCHAR(3),
    document_number VARCHAR(11),
    contact_email   VARCHAR(254),
    CONSTRAINT uk_clients_document_number UNIQUE (document_number),
    -- tipo y número de documento van juntos: los dos o ninguno
    CONSTRAINT ck_clients_document_pair CHECK ((document_type IS NULL) = (document_number IS NULL)),
    CONSTRAINT ck_clients_document CHECK (
        document_type IS NULL
        OR (document_type = 'RUC' AND document_number ~ '^[0-9]{11}$')
        OR (document_type = 'DNI' AND document_number ~ '^[0-9]{8}$')
    )
);

CREATE TABLE equipment (
    id            BIGSERIAL    PRIMARY KEY,
    client_id     BIGINT       NOT NULL,
    model         VARCHAR(100) NOT NULL,
    serial_number VARCHAR(255),
    CONSTRAINT fk_equipment_client FOREIGN KEY (client_id) REFERENCES clients (id)
);

CREATE INDEX ix_equipment_client_id ON equipment (client_id);

CREATE TABLE catalogs (
    id     BIGSERIAL    PRIMARY KEY,
    type   VARCHAR(20)  NOT NULL,
    value  VARCHAR(150) NOT NULL,
    active BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_catalogs_type_value UNIQUE (type, value),
    CONSTRAINT ck_catalogs_type CHECK (type IN ('ACTION', 'STATUS', 'EVENT_FAILURE', 'PERSONNEL', 'SUPERVISOR'))
);

CREATE TABLE technical_reports (
    id                 BIGSERIAL    PRIMARY KEY,
    report_number      VARCHAR(20)  NOT NULL,
    equipment_id       BIGINT       NOT NULL,
    start_datetime     TIMESTAMP,
    end_datetime       TIMESTAMP    NOT NULL,
    initial_status_id  BIGINT       NOT NULL,
    final_status_id    BIGINT       NOT NULL,
    event_failure_id   BIGINT       NOT NULL,
    affected_component VARCHAR(255),
    action_id          BIGINT       NOT NULL,
    details            TEXT         NOT NULL,
    personnel_id       BIGINT       NOT NULL,
    supervisor_id      BIGINT       NOT NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by         BIGINT       NOT NULL,
    CONSTRAINT uk_technical_reports_report_number UNIQUE (report_number),
    CONSTRAINT ck_technical_reports_dates CHECK (start_datetime IS NULL OR end_datetime >= start_datetime),
    CONSTRAINT fk_technical_reports_equipment FOREIGN KEY (equipment_id) REFERENCES equipment (id),
    CONSTRAINT fk_technical_reports_initial_status FOREIGN KEY (initial_status_id) REFERENCES catalogs (id),
    CONSTRAINT fk_technical_reports_final_status FOREIGN KEY (final_status_id) REFERENCES catalogs (id),
    CONSTRAINT fk_technical_reports_event_failure FOREIGN KEY (event_failure_id) REFERENCES catalogs (id),
    CONSTRAINT fk_technical_reports_action FOREIGN KEY (action_id) REFERENCES catalogs (id),
    CONSTRAINT fk_technical_reports_personnel FOREIGN KEY (personnel_id) REFERENCES catalogs (id),
    CONSTRAINT fk_technical_reports_supervisor FOREIGN KEY (supervisor_id) REFERENCES catalogs (id),
    CONSTRAINT fk_technical_reports_created_by FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE INDEX ix_technical_reports_equipment_id ON technical_reports (equipment_id);
CREATE INDEX ix_technical_reports_end_datetime ON technical_reports (end_datetime);

CREATE TABLE report_counters (
    series      VARCHAR(10) PRIMARY KEY,
    last_number INTEGER     NOT NULL DEFAULT 0,
    CONSTRAINT ck_report_counters_last_number CHECK (last_number >= 0)
);

-- Serie única de la empresa (DESIGN.md, sección 7)
INSERT INTO report_counters (series, last_number) VALUES ('008', 0);
