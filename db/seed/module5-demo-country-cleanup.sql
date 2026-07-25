-- Module 5 presentation country cleanup
-- Removes countries with no active published lectures from booking/profit country statistics
-- by reassigning their test accommodations/packages to a real active lecture country.
-- Current dump case: 대한민국 appears through CPS test accommodation bookings, not lectures.

START TRANSACTION;

SET @target_country_id := (
    SELECT c.country_id
    FROM countries c
    WHERE c.name = '일본'
      AND c.is_active = true
    LIMIT 1
);

SET @fallback_country_id := (
    SELECT l.country_id
    FROM lectures l
    JOIN countries c ON c.country_id = l.country_id AND c.is_active = true
    WHERE l.status = 'PUBLISHED'
      AND COALESCE(l.is_deleted, false) = false
      AND c.name NOT IN ('대한민국', '한국', 'Republic of Korea', 'South Korea', 'Korea')
      AND EXISTS (
          SELECT 1
          FROM accommodations a
          WHERE a.country_id = l.country_id
      )
    GROUP BY l.country_id
    ORDER BY COUNT(*) DESC, l.country_id
    LIMIT 1
);

SET @presentation_country_id := COALESCE(@target_country_id, @fallback_country_id);

CREATE TABLE IF NOT EXISTS m5_country_cleanup_backup_accommodations_20260725 AS
SELECT a.*
FROM accommodations a
JOIN countries c ON c.country_id = a.country_id
WHERE c.name IN ('대한민국', '한국', 'Republic of Korea', 'South Korea', 'Korea')
  AND NOT EXISTS (
      SELECT 1
      FROM lectures l
      WHERE l.country_id = a.country_id
        AND l.status = 'PUBLISHED'
        AND COALESCE(l.is_deleted, false) = false
  );

CREATE TABLE IF NOT EXISTS m5_country_cleanup_backup_packages_20260725 AS
SELECT p.*
FROM packages p
JOIN countries c ON c.country_id = p.country_id
WHERE c.name IN ('대한민국', '한국', 'Republic of Korea', 'South Korea', 'Korea')
  AND NOT EXISTS (
      SELECT 1
      FROM lectures l
      WHERE l.country_id = p.country_id
        AND l.status = 'PUBLISHED'
        AND COALESCE(l.is_deleted, false) = false
  );

UPDATE accommodations a
JOIN countries c ON c.country_id = a.country_id
SET a.country_id = @presentation_country_id
WHERE @presentation_country_id IS NOT NULL
  AND c.name IN ('대한민국', '한국', 'Republic of Korea', 'South Korea', 'Korea')
  AND NOT EXISTS (
      SELECT 1
      FROM lectures l
      WHERE l.country_id = c.country_id
        AND l.status = 'PUBLISHED'
        AND COALESCE(l.is_deleted, false) = false
  );

UPDATE packages p
JOIN countries c ON c.country_id = p.country_id
SET p.country_id = @presentation_country_id
WHERE @presentation_country_id IS NOT NULL
  AND c.name IN ('대한민국', '한국', 'Republic of Korea', 'South Korea', 'Korea')
  AND NOT EXISTS (
      SELECT 1
      FROM lectures l
      WHERE l.country_id = c.country_id
        AND l.status = 'PUBLISHED'
        AND COALESCE(l.is_deleted, false) = false
  );

-- Validation: should return 0 rows for countries that have booking/profit source data
-- but no active published lecture.
SELECT c.country_id,
       c.name,
       COUNT(DISTINCT b.booking_id) AS booking_count
FROM countries c
JOIN accommodations a ON a.country_id = c.country_id
JOIN bookings b ON b.accommodation_id = a.accommodation_id
WHERE NOT EXISTS (
    SELECT 1
    FROM lectures l
    WHERE l.country_id = c.country_id
      AND l.status = 'PUBLISHED'
      AND COALESCE(l.is_deleted, false) = false
)
GROUP BY c.country_id, c.name;

COMMIT;