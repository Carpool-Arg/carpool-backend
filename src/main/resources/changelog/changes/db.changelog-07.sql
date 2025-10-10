--liquibase formatted sql

--changeset AgustinAnil:insercion_valor_state
insert into state (name, scope) values ('CREATE', 'TRIP')