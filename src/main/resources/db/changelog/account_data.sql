--liquibase formatted sql

--changeset dakorshik:init account data

INSERT INTO public.account (id, username, firstname, lastname, tg_id) VALUES (1, 'admin', 'admin', 'admin', 0);
