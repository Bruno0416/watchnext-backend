-- feedback-service: esquema inicial en mysql (mismo que genera hibernate con las entidades)

create table rating (
    score float(53) not null,
    tmdb_id integer not null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    id binary(16) not null,
    user_id varchar(255) not null,
    media_type enum ('ALL','MOVIE','TV') not null,
    primary key (id),
    constraint uk_user_content_rating unique (user_id, tmdb_id, media_type)
) engine=InnoDB;

create table review (
    tmdb_id integer not null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    id binary(16) not null,
    body text not null,
    user_id varchar(255) not null,
    media_type enum ('ALL','MOVIE','TV') not null,
    primary key (id),
    constraint uk_user_content_review unique (user_id, tmdb_id, media_type)
) engine=InnoDB;
