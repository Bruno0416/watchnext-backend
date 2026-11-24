-- user-service: esquema inicial (igual al que hibernate creaba con ddl-auto)
-- en las bases de neon que ya tienen tablas flyway lo marca como baseline y no lo corre

create table if not exists favorite_content (
    position integer not null check ((position>=0)),
    tmdb_id integer not null,
    media_type varchar(10) not null check ((media_type in ('MOVIE','TV','ALL'))),
    profile_id uuid not null,
    primary key (position, profile_id),
    constraint uk_favorite_profile_content unique (profile_id, tmdb_id, media_type)
);

create table if not exists follows (
    created_at timestamp(6) with time zone not null,
    status varchar(10) not null check ((status in ('PENDING','ACCEPTED'))),
    followee_profile_id uuid not null,
    follower_profile_id uuid not null,
    id uuid not null,
    primary key (id),
    constraint uk_follow_pair unique (follower_profile_id, followee_profile_id),
    constraint chk_no_self_follow check (follower_profile_id <> followee_profile_id)
);

create table if not exists profiles (
    country varchar(2),
    followers_count integer not null,
    following_count integer not null,
    onboarding_completed boolean not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    id uuid not null,
    region varchar(16),
    visibility varchar(16) not null check ((visibility in ('PRIVATE','PUBLIC'))),
    username varchar(30) not null,
    display_name varchar(50),
    bio varchar(500),
    avatar_url varchar(512),
    user_id varchar(255) not null,
    primary key (id),
    constraint uk_profiles_user_id unique (user_id),
    constraint uk_profiles_username unique (username)
);

create index if not exists idx_follows_followee_status 
   on follows (followee_profile_id, status);

create index if not exists idx_follows_follower_status 
   on follows (follower_profile_id, status);

alter table if exists favorite_content 
   add constraint fk_favorite_profile 
   foreign key (profile_id) 
   references profiles;

alter table if exists follows 
   add constraint fk_follow_followee 
   foreign key (followee_profile_id) 
   references profiles;

alter table if exists follows 
   add constraint fk_follow_follower 
   foreign key (follower_profile_id) 
   references profiles;
