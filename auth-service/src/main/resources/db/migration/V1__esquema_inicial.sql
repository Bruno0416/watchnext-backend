-- auth-service: esquema inicial en mysql (mismo que genera hibernate con las entidades)

create table users (
    email_verified bit not null,
    created_at datetime(6) not null,
    id binary(16) not null,
    password varchar(100),
    email varchar(150) not null,
    role enum ('ADMIN','USER') not null,
    primary key (id),
    constraint uk_users_email unique (email)
) engine=InnoDB;

create table user_identities (
    created_at datetime(6) not null,
    id binary(16) not null,
    user_id binary(16) not null,
    email varchar(150),
    provider_user_id varchar(255) not null,
    provider enum ('APPLE','GOOGLE') not null,
    primary key (id),
    constraint uk_provider_provider_user_id unique (provider, provider_user_id),
    constraint fk_user_identity_user foreign key (user_id) references users (id)
) engine=InnoDB;

create index idx_user_identities_user_id on user_identities (user_id);

create table verification_codes (
    used bit not null,
    code varchar(6) not null,
    created_at datetime(6) not null,
    expires_at datetime(6) not null,
    id binary(16) not null,
    email varchar(255) not null,
    type enum ('CONFIRMATION','PASSWORD_RECOVERY') not null,
    primary key (id)
) engine=InnoDB;

create index idx_verification_codes_email_type on verification_codes (email, type);
