--liquibase formatted sql

--changeset GonzaloBaldassi:insercion_valor_state_reservation
insert into state (name, scope, finish) values ('PENDING', 'RESERVATION', false);
insert into state (name, scope, finish) values ('ACCEPTED', 'RESERVATION', false);
insert into state (name, scope, finish) values ('IN_PROGRESS', 'RESERVATION', false);
insert into state (name, scope, finish) values ('UNPAID', 'RESERVATION', false);
insert into state (name, scope, finish) values ('REJECTED', 'RESERVATION', true);
insert into state (name, scope, finish) values ('CANCELLED', 'RESERVATION', true);
insert into state (name, scope, finish) values ('COMPLETED', 'RESERVATION', true);
insert into state (name, scope, finish) values ('EXPIRED', 'RESERVATION', true);

insert into state (name, scope, finish) values ('CANCELLED', 'TRIP', true);
insert into state (name, scope, finish) values ('IN_PROGRESS', 'TRIP', false);
insert into state (name, scope, finish) values ('FINISHED', 'TRIP', true);
insert into state (name, scope, finish) values ('CLOSED', 'TRIP', false);


