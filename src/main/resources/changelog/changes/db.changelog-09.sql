--liquibase formatted sql

--changeset SantiagoGodoy:Insercion_Ciudad_Por_Defecto
INSERT INTO configurations_settings (key_name, key_value, description)
VALUES ('default-city-id', '409', 'ID de la ciudad usado para cargar el feed inicial cuando la ubicacion del usuario no esta disponible.')
ON CONFLICT (key_name) DO NOTHING;


