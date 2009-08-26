--
-- Title:      Property Value tables
-- Database:   MySQL InnoDB
-- Since:      V3.2 Schema 3001
-- Author:     Derek Hulley
--
-- Please contact support@alfresco.com if you need assistance with the upgrade.
--

CREATE TABLE alf_prop_class
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   java_class_name VARCHAR(255) NOT NULL,
   java_class_name_short VARCHAR(32) NOT NULL,
   java_class_name_crc BIGINT NOT NULL,
   UNIQUE INDEX idx_alf_prop_class_crc (java_class_name_crc, java_class_name_short),
   INDEX idx_alf_prop_class_class (java_class_name),
   PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE alf_prop_date_value
(
   date_value BIGINT NOT NULL,
   full_year SMALLINT NOT NULL,
   half_of_year TINYINT NOT NULL,
   quarter_of_year TINYINT NOT NULL,
   month_of_year TINYINT NOT NULL,
   week_of_year TINYINT NOT NULL,
   week_of_month TINYINT NOT NULL,
   day_of_year SMALLINT NOT NULL,
   day_of_month TINYINT NOT NULL,
   day_of_week TINYINT NOT NULL,
   INDEX idx_alf_prop_date_units (full_year, month_of_year, day_of_month),
   PRIMARY KEY (date_value)
) ENGINE=InnoDB;

CREATE TABLE alf_prop_double_value
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   double_value DOUBLE NOT NULL,
   INDEX idx_alf_prop_dbl_val (double_value),
   PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE alf_prop_string_value
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   string_value TEXT NOT NULL,
   string_end_lower VARCHAR(16) NOT NULL,
   string_crc BIGINT NOT NULL,
   INDEX idx_alf_prop_str (string_value(32)),
   INDEX idx_alf_prop_crc (string_end_lower, string_crc),
   PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE alf_prop_serializable_value
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   serializable_value BLOB NOT NULL,
   PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE alf_prop_value
(
   id BIGINT NOT NULL AUTO_INCREMENT,
   actual_type_id BIGINT NOT NULL,
   persisted_type TINYINT NOT NULL,
   long_value BIGINT NOT NULL,
   INDEX idx_alf_prop_per (persisted_type, long_value),
   INDEX idx_alf_prop_act (actual_type_id, long_value),
   PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE alf_prop_link
(
   root_prop_id BIGINT NOT NULL,
   current_prop_id BIGINT NOT NULL,
   value_prop_id BIGINT NOT NULL,
   key_prop_id BIGINT NOT NULL,
   INDEX idx_alf_prop_coll_rev (value_prop_id, root_prop_id),
   PRIMARY KEY (root_prop_id, current_prop_id, value_prop_id, key_prop_id)
) ENGINE=InnoDB;

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-V3.2-PropertyValueTables';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-V3.2-PropertyValueTables', 'Manually executed script upgrade V3.2: PropertyValue Tables',
    0, 3000, -1, 3001, null, 'UNKOWN', 1, 1, 'Script completed'
  );