-- list-service: esquema inicial en mysql (mismo que genera hibernate con las entidades)

create table user_list (
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    id binary(16) not null,
    name varchar(80) not null,
    description varchar(200),
    user_id varchar(255) not null,
    visibility enum ('PRIVATE','PUBLIC') not null,
    primary key (id)
) engine=InnoDB;

create table list_item (
    position integer not null,
    tmdb_id integer not null,
    added_at datetime(6) not null,
    id binary(16) not null,
    list_id binary(16) not null,
    media_type enum ('ALL','MOVIE','TV') not null,
    primary key (id),
    constraint uk_list_content unique (list_id, tmdb_id, media_type),
    constraint fk_list_item_list foreign key (list_id) references user_list (id) on delete cascade
) engine=InnoDB;
