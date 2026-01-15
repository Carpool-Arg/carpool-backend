--liquibase formatted sql

--changeset GonzaloBaldassi:insercion_valor_state_reservation
insert into state (name, scope) values ('PENDING', 'RESERVATION');
insert into state (name, scope) values ('ACCEPTED', 'RESERVATION');
insert into state (name, scope) values ('REJECTED', 'RESERVATION');
insert into state (name, scope) values ('CANCELLED', 'RESERVATION');
insert into state (name, scope) values ('COMPLETED', 'RESERVATION');
insert into state (name, scope) values ('CANCELLED', 'TRIP');
insert into state (name, scope) values ('IN_PROGRESS', 'TRIP');
insert into state (name, scope) values ('FINISHED', 'TRIP');