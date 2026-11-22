-- Les "jobs" deviennent des "ordres" d'arc-publisher ; temporalité des comptes et des profils :
-- date de début (création différée), date de fin (suppression planifiée) et ordre de suppression associé.
ALTER TABLE account RENAME COLUMN job_id TO order_id;
ALTER TABLE account ADD COLUMN start_date DATE;
ALTER TABLE account ADD COLUMN end_date DATE;
ALTER TABLE account ADD COLUMN end_order_id UUID;

ALTER TABLE profile RENAME COLUMN job_id TO order_id;
ALTER TABLE profile ADD COLUMN start_date DATE;
ALTER TABLE profile ADD COLUMN end_date DATE;
ALTER TABLE profile ADD COLUMN end_order_id UUID;
