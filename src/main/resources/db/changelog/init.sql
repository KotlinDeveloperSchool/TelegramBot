--liquibase formatted sql

--changeset dakorshik:init


create table user1
(
    id bigserial constraint user_pk primary key,
    tg_id int not null unique,
    name varchar(100)
);
