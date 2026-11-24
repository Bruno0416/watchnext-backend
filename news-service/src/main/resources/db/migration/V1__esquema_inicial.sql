-- news-service: tabla de noticias en mysql (mismo que genera hibernate con la entidad)

create table news (
    created_at datetime(6) not null,
    published_at datetime(6) not null,
    updated_at datetime(6) not null,
    id binary(16) not null,
    title varchar(150) not null,
    slug varchar(160) not null,
    summary varchar(300) not null,
    image_url varchar(512),
    content text not null,
    primary key (id),
    constraint uk_news_slug unique (slug)
) engine=InnoDB;

create index idx_news_published_at on news (published_at);
