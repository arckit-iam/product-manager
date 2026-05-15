CREATE TABLE product (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uq_product_name UNIQUE (name)
);

CREATE TABLE profile (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT NOT NULL,
    name        VARCHAR(255),
    description VARCHAR(255)
);

CREATE TABLE resource_type (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255),
    description VARCHAR(255)
);

CREATE TABLE ressource (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255),
    criticite        VARCHAR(10),
    resource_type_id BIGINT NOT NULL,
    product_id       BIGINT NOT NULL,
    CONSTRAINT fk_ressource_resource_type FOREIGN KEY (resource_type_id) REFERENCES resource_type (id),
    CONSTRAINT fk_ressource_product       FOREIGN KEY (product_id)       REFERENCES product (id)
);

CREATE TABLE permission_type (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(255),
    description      VARCHAR(255),
    resource_type_id BIGINT NOT NULL,
    CONSTRAINT fk_permission_type_resource_type FOREIGN KEY (resource_type_id) REFERENCES resource_type (id)
);

CREATE TABLE permission (
    id                 BIGSERIAL PRIMARY KEY,
    profile_id         BIGINT NOT NULL,
    ressource_id       BIGINT NOT NULL,
    permission_type_id BIGINT NOT NULL,
    CONSTRAINT fk_permission_profile         FOREIGN KEY (profile_id)         REFERENCES profile (id),
    CONSTRAINT fk_permission_ressource       FOREIGN KEY (ressource_id)       REFERENCES ressource (id),
    CONSTRAINT fk_permission_permission_type FOREIGN KEY (permission_type_id) REFERENCES permission_type (id)
);
