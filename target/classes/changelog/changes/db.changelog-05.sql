--liquibase formatted sql

--changeset AgustinAnil:crear_tabla_media
CREATE UNIQUE INDEX uq_media_profile_primary
  ON media(user_id)
  WHERE category = 'PROFILE';