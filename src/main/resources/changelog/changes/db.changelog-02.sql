--liquibase formatted sql

--changeset RoberSalera:crear_rol_usuario_por_defecto_01
INSERT INTO public."role" (id, "name") VALUES(1, 'ROLE_USER');
