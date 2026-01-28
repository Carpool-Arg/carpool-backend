--liquibase formatted sql

--changeset AgustinAnil:modificar_columnas_tabla_user_01
COMMENT ON COLUMN public.users.dni IS 'DNI o Documento de Identificacion de la persona';
COMMENT ON COLUMN public.users.deleted_at IS 'Fecha de eliminacion del registro';
COMMENT ON COLUMN public.users.created_at IS 'Fecha de creacion del registro';
COMMENT ON COLUMN public.users.deleted_by IS 'Usuario que elimino el registro';
COMMENT ON COLUMN public.users.updated_at IS 'Fecha de actualizacion del registro';
