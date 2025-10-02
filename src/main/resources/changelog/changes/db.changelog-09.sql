--liquibase formatted sql

--changeset SantiagoGodoy:actuaizar_la_tabla_Driver_anadiendo_rating
ALTER TABLE public.driver ADD COLUMN rating DOUBLE PRECISION;