--liquibase formatted sql

--changeset AgustinAnil:crear_tabla_media
CREATE UNIQUE INDEX uq_media_profile_primary
  ON media(user_id)
  WHERE category = 'PROFILE';

-- Esto permite que cada usuario tenga unicamente un registro del tipo PROFILE en la abse de datos