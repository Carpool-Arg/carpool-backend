--liquibase formatted sql

--changeset AgustinAnil:insercion_valor_state_reservation_02
insert into state (name, scope) values ('ACCEPTED', 'RESERVATION');
insert into state (name, scope) values ('REJECTED', 'RESERVATION');
insert into state (name, scope) values ('CANCELLED', 'RESERVATION');
insert into state (name, scope) values ('COMPLETED', 'RESERVATION');
insert into state (name, scope) values ('IN_PROGRESS', 'RESERVATION');
insert into state (name, scope) values ('UNPAID', 'RESERVATION');