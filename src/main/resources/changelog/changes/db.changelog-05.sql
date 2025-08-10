--liquibase formatted sql

--changeset AgustinAnil:crear_tabla_assets
create table media_asset (
id int8 primary key,
owner int8,
bucket varchar(50) not null,
object_key text not null,
filename text not null,
content_type varchar(64) not null,
bytes_size bigint not null,
created_at timestamp not null default current_timestamp,
unique (bucket, object_key),
constraint fk_media_asset_user foreign key (owner) references "users"(id) on delete cascade)

COMMENT ON COLUMN public.media_asset."owner" IS 'A quien corresponde la imagen, puede ser a un usuario o a la app';
COMMENT ON COLUMN public.media_asset.bucket IS 'Bucket de Cloudflare';
COMMENT ON COLUMN public.media_asset.object_key IS 'Identificador de la imagen en el bucket';
COMMENT ON COLUMN public.media_asset.filename IS 'Nombre del archivo';
COMMENT ON COLUMN public.media_asset.content_type IS 'Tipo de contenido de la imagen';
COMMENT ON COLUMN public.media_asset.bytes_size IS 'Tamanio';
COMMENT ON COLUMN public.media_asset.created_at IS 'Fecha de cracion';