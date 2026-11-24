-- user-service: esquema inicial en mysql (mismo que genera hibernate con las entidades)

create table profiles (
    country varchar(2),
    followers_count integer not null,
    following_count integer not null,
    onboarding_completed bit not null,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    id binary(16) not null,
    region varchar(16),
    username varchar(30) not null,
    display_name varchar(50),
    bio varchar(500),
    avatar_url varchar(512),
    user_id varchar(255) not null,
    visibility enum ('PRIVATE','PUBLIC') not null,
    primary key (id),
    constraint uk_profiles_user_id unique (user_id),
    constraint uk_profiles_username unique (username)
) engine=InnoDB;

create table favorite_content (
    position integer not null,
    tmdb_id integer not null,
    profile_id binary(16) not null,
    media_type enum ('ALL','MOVIE','TV') not null,
    primary key (position, profile_id),
    constraint uk_favorite_profile_content unique (profile_id, tmdb_id, media_type),
    constraint chk_favorite_position check (position >= 0),
    constraint fk_favorite_profile foreign key (profile_id) references profiles (id)
) engine=InnoDB;

create table follows (
    created_at datetime(6) not null,
    followee_profile_id binary(16) not null,
    follower_profile_id binary(16) not null,
    id binary(16) not null,
    status enum ('ACCEPTED','PENDING') not null,
    primary key (id),
    constraint uk_follow_pair unique (follower_profile_id, followee_profile_id),
    constraint chk_no_self_follow check (follower_profile_id <> followee_profile_id),
    constraint fk_follow_followee foreign key (followee_profile_id) references profiles (id),
    constraint fk_follow_follower foreign key (follower_profile_id) references profiles (id)
) engine=InnoDB;

create index idx_follows_followee_status on follows (followee_profile_id, status);

create index idx_follows_follower_status on follows (follower_profile_id, status);
