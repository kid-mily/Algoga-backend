-- Rollback for module5-demo-country-cleanup.sql

START TRANSACTION;

UPDATE accommodations a
JOIN m5_country_cleanup_backup_accommodations_20260725 b
  ON b.accommodation_id = a.accommodation_id
SET a.country_id = b.country_id;

UPDATE packages p
JOIN m5_country_cleanup_backup_packages_20260725 b
  ON b.package_id = p.package_id
SET p.country_id = b.country_id;

-- Backup tables are kept intentionally.
-- DROP TABLE IF EXISTS m5_country_cleanup_backup_accommodations_20260725;
-- DROP TABLE IF EXISTS m5_country_cleanup_backup_packages_20260725;

COMMIT;