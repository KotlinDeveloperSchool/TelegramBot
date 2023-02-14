--liquibase formatted sql

--changeset dakorshik:init account data

INSERT INTO public.account (id, username, firstname, lastname)
    VALUES (1, 'admin', 'admin', 'admin');
