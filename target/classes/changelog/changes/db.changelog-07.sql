--liquibase formatted sql

--changeset SantiagoGodoy:insercion_valor_state
insert into state (name, scope) values ('CREATE', 'TRIP')