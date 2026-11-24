-- feedback-service: esquema inicial (igual al que hibernate creaba con ddl-auto)
-- en las bases de neon que ya tienen tablas flyway lo marca como baseline y no lo corre

create table if not exists rating (
    score float(53) not null,
    tmdb_id integer not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    media_type varchar(10) not null check ((media_type in ('MOVIE','TV','ALL'))),
    id uuid not null,
    user_id varchar(255) not null,
    primary key (id),
    constraint uk_user_content_rating unique (user_id, tmdb_id, media_type)
);

create table if not exists review (
    tmdb_id integer not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    media_type varchar(10) not null check ((media_type in ('MOVIE','TV','ALL'))),
    id uuid not null,
    body TEXT not null,
    user_id varchar(255) not null,
    primary key (id),
    constraint uk_user_content_review unique (user_id, tmdb_id, media_type)
);
