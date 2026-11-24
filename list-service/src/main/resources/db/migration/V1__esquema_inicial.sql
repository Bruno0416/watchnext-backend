-- list-service: esquema inicial (igual al que hibernate creaba con ddl-auto)
-- en las bases de neon que ya tienen tablas flyway lo marca como baseline y no lo corre

create table if not exists list_item (
    position integer not null,
    tmdb_id integer not null,
    added_at timestamp(6) with time zone not null,
    media_type varchar(10) not null check ((media_type in ('MOVIE','TV','ALL'))),
    id uuid not null,
    list_id uuid not null,
    primary key (id),
    constraint uk_list_content unique (list_id, tmdb_id, media_type)
);

create table if not exists user_list (
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    visibility varchar(10) not null check ((visibility in ('PUBLIC','PRIVATE'))),
    id uuid not null,
    name varchar(80) not null,
    description varchar(200),
    user_id varchar(255) not null,
    primary key (id)
);

alter table if exists list_item 
   add constraint fk_list_item_list 
   foreign key (list_id) 
   references user_list 
   on delete cascade;
