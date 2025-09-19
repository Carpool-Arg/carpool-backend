--liquibase formatted sql

--changeset SantiagoGodoy:actualizar_el_valor_de_Trip_a_TRIP_segun_el_changelog. 
UPDATE state SET scope = 'TRIP' WHERE name = 'CREATE' AND scope = 'Trip';