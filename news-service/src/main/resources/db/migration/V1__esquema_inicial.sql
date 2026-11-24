-- news-service: tabla de noticias

create table if not exists news (
    id uuid not null,
    slug varchar(160) not null,
    title varchar(150) not null,
    summary varchar(300) not null,
    content TEXT not null,
    image_url varchar(512),
    published_at timestamp(6) with time zone not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id),
    constraint uk_news_slug unique (slug)
);

create index if not exists idx_news_published_at
   on news (published_at);
