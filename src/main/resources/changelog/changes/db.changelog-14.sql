--liquibase formatted sql

--changeset SantiagoGodoy:Insercion_Precio_Minimo
INSERT INTO configurations_settings (key_name, key_value, description)
VALUES ('minimun-price-value', '2000', 'Precio minimo necesario para publicar un viaje.')
    ON CONFLICT (key_name) DO NOTHING;