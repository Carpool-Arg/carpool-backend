--liquibase formatted sql

--changeset SantiagoGodoy:Insercion_Ciudad_Por_Defecto
INSERT INTO config_parameters (key_name, key_value, description)
VALUES ('default-city-id', '409', 'ID de la ciudad usado para cargar el feed inicial cuando la ubicacion del usuario no esta disponible.'),
       ('discount-percentage', '5', 'Porcentaje de descuento que se aplica al precio publicado por los choferes a los viajes y se suma a las reservas de los pasajeros.'),
       ('minimum-city-distance', '15', 'Distancia mínima que tenes que cumplir para que te retorne una localidad.');
