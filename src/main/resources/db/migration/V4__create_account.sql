-- Comptes non utilisateurs des produits. Les métadonnées sont libres côté base (JSONB) : leurs champs
-- autorisés sont décrits par le type de compte, défini dans la configuration du déploiement.
CREATE TABLE account (
    id          BIGSERIAL PRIMARY KEY,
    identifier  VARCHAR(255) NOT NULL UNIQUE,
    type        VARCHAR(100) NOT NULL,
    product_id  BIGINT REFERENCES product (id) ON DELETE SET NULL,
    source      VARCHAR(20)  NOT NULL,
    metadata    JSONB        NOT NULL DEFAULT '{}'::jsonb,
    job_id      UUID,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_account_product ON account (product_id);
CREATE INDEX idx_account_type ON account (type);
CREATE INDEX idx_account_metadata ON account USING GIN (metadata);
