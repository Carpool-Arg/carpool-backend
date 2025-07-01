--liquibase formatted sql

--changeset AgustinAnil:modificar_columnas_tabla_user_01
ALTER TABLE public.users ALTER COLUMN email TYPE varchar(75) USING email::varchar(75);
ALTER TABLE public.users ALTER COLUMN lastname TYPE varchar(100) USING lastname::varchar(100);
ALTER TABLE public.users ALTER COLUMN name TYPE varchar(100) USING name::varchar(100);
ALTER TABLE public.users ALTER COLUMN phone TYPE varchar(25) USING phone::varchar(25);
ALTER TABLE public.users ALTER COLUMN username TYPE varchar(25) USING username::varchar(25);
ALTER TABLE public.users ALTER COLUMN dni TYPE varchar(50) USING dni::varchar(50);
ALTER TABLE public.users ALTER COLUMN dni SET NOT NULL;
COMMENT ON COLUMN public.users.dni IS 'DNI o Documento de Identificacion de la persona';
ALTER TABLE public.users ALTER COLUMN email SET NOT NULL;
ALTER TABLE public.users ALTER COLUMN lastname SET NOT NULL;
ALTER TABLE public.users ALTER COLUMN "name" SET NOT NULL;
ALTER TABLE public.users ALTER COLUMN "password" SET NOT NULL;
ALTER TABLE public.users ALTER COLUMN phone SET NOT NULL;
ALTER TABLE public.users ALTER COLUMN username SET NOT NULL;
ALTER TABLE public.users ALTER COLUMN deleted_at DROP NOT NULL;
COMMENT ON COLUMN public.users.deleted_at IS 'Fecha de eliminacion del registro';
COMMENT ON COLUMN public.users.created_at IS 'Fecha de creacion del registro';
COMMENT ON COLUMN public.users.deleted_by IS 'Usuario que elimino el registro';
COMMENT ON COLUMN public.users.updated_at IS 'Fecha de actualizacion del registro';

--changeset AgustinAnil:modificar_columnas_tabla_role_01
ALTER TABLE public."role" ALTER COLUMN "name" TYPE varchar(100) USING "name"::varchar(100);
ALTER TABLE public."role" ALTER COLUMN "name" SET NOT NULL;