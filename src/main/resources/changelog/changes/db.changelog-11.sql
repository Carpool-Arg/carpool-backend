--liquibase formatted sql

--changeset AnilAgustin:Insercion_Parametro_Velocidad
INSERT INTO public.config_parameters
(id, description, key_name, key_value)
VALUES(4, 'Velocidad promedio utilizada en la aplicacion para la realizacion de calculos', 'average-speed-kmh', '80');