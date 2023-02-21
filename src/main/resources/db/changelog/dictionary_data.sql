--liquibase formatted sql

--changeset dakorshik:init dictionary data

INSERT INTO public.dictionary (name, owner_id) VALUES ('Цвета', 1);
INSERT INTO public.dictionary (name, owner_id) VALUES ('Внешность и характер', 1);
INSERT INTO public.dictionary (name, owner_id) VALUES ('Действия и движения', 1);
INSERT INTO public.dictionary (name, owner_id) VALUES ('Инструменты', 1);
INSERT INTO public.dictionary (name, owner_id) VALUES ('Посуда', 1);
INSERT INTO public.dictionary (name, owner_id) VALUES ('Эмоции', 1);
INSERT INTO public.dictionary (name, owner_id) VALUES ('Спальная комната', 1);
INSERT INTO public.dictionary (name, owner_id) VALUES ('Птицы', 1);
INSERT INTO public.dictionary (name, owner_id) VALUES ('Спорт', 1);
INSERT INTO public.dictionary (name, owner_id) VALUES ('Напитки', 1);
INSERT INTO public.dictionary (name, owner_id) VALUES ('Ванная комната', 1);
INSERT INTO public.dictionary (name, owner_id) VALUES ('Деньги', 1);
INSERT INTO public.dictionary (name, owner_id) VALUES ('Дом', 1);
INSERT INTO public.dictionary (name, owner_id) VALUES ('Школа', 1);
INSERT INTO public.dictionary (name, owner_id) VALUES ('География', 1);
