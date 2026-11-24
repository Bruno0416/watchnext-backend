-- auth-service: esquema inicial (igual al que hibernate creaba con ddl-auto)
-- en las bases de neon que ya tienen tablas flyway lo marca como baseline y no lo corre

create table if not exists user_identities (
    created_at timestamp(6) with time zone not null,
    id uuid not null,
    user_id uuid not null,
    provider varchar(20) not null check ((provider in ('GOOGLE','APPLE'))),
    email varchar(150),
    provider_user_id varchar(255) not null,
    primary key (id),
    constraint uk_provider_provider_user_id unique (provider, provider_user_id)
);

create table if not exists users (
    email_verified boolean not null,
    created_at timestamp(6) not null,
    id uuid not null,
    password varchar(100),
    email varchar(150) not null unique,
    role varchar(255) not null check ((role in ('USER','ADMIN'))),
    primary key (id)
);

create table if not exists verification_codes (
    used boolean not null,
    code varchar(6) not null,
    created_at timestamp(6) with time zone not null,
    expires_at timestamp(6) with time zone not null,
    id uuid not null,
    email varchar(255) not null,
    type varchar(255) not null check ((type in ('CONFIRMATION','PASSWORD_RECOVERY'))),
    primary key (id)
);

create index if not exists idx_user_identities_user_id 
   on user_identities (user_id);

create index if not exists idx_verification_codes_email_type 
   on verification_codes (email, type);

alter table if exists user_identities 
   add constraint fk_user_identity_user 
   foreign key (user_id) 
   references users;
