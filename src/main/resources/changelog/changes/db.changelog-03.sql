--liquibase formatted sql

--changeset SantiGodoy:crear_rol_chofer_por_defecto_01
INSERT INTO public."role" (id, "name") VALUES(2, 'ROLE_DRIVER') ON CONFLICT (id) DO NOTHING;
