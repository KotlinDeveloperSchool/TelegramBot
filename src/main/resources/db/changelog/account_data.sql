--liquibase formatted sql

--changeset dakorshik:init_account_data

INSERT INTO public.account (id, username, firstname, lastname)
    VALUES (1, 'admin', 'admin', 'admin');
