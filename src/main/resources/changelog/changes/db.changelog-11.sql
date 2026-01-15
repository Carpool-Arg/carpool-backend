--liquibase formatted sql

--changeset RobertoSalera:insercion_valor_state_reservation_02
insert into state (name, scope) values ('CLOSED', 'TRIP');
insert into state (name, scope) values ('CANCELLED', 'TRIP');
insert into state (name, scope) values ('IN_PROGRESS', 'TRIP');
insert into state (name, scope) values ('FINISHED', 'TRIP');