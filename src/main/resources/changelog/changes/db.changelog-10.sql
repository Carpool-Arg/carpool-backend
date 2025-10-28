--liquibase formatted sql

--changeset GonzaloBaldassi:insercion_valor_state_reservation
insert into state (name, scope) values ('PENDING', 'RESERVATION')