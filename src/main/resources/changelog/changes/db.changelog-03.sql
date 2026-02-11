--liquibase formatted sql

--changeset SantiGodoy:crear_tipos_de_vehiculos_por_defecto_01
INSERT INTO public."vehicle_type" (id, "name", "description") VALUES 
(1, 'AUTO', 'VEHICULO DE USO PERSONAL ESTANDAR'),
(2, 'SUV', 'VEHICULO FAMILIAR'),
(3, 'PICKUP', 'CAMIONETA CON CAJA TRASERA')
ON CONFLICT (id) DO NOTHING;


