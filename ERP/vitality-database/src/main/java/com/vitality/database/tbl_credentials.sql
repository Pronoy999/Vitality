alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_credentials;
create table vitality.tbl_credentials
(
    id                bigint primary key not null default nextval('global_sid_seq'),
    user_id           varchar(1000)      not null,
    email_id          varchar(1000)               default null,
    phone_number      varchar(20)                 default null,
    password          text                        default null,
    google_token      text                        default null,
    is_active         boolean            not null default true,
    created_timestamp timestamp          not null default current_timestamp,
    updated_timestamp timestamp          not null default current_timestamp
);
alter table tbl_credentials
    add constraint fk_user_id foreign key (user_id)
        references vitality.tbl_users (guid);