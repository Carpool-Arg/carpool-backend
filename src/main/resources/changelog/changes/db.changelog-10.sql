--liquibase formatted sql

--changeset GonzaloBaldassi:insercion_valor_state
insert into state (name, scope) values ('IN_PROGRESS', 'Trip');
insert into state (name, scope) values ('CLOSED', 'Trip');
insert into state (name, scope) values ('CANCELLED', 'Trip');
insert into state (name, scope) values ('FINISHED', 'Trip');