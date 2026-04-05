alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_users;
create table vitality.tbl_users
(
    guid              varchar(1000) not null primary key,
    first_name        varchar(1000) not null,
    last_name         varchar(1000) not null,
    date_of_birth     date                   default null,
    gender            varchar(10)   not null,
    age               decimal(10, 0)         default null,
    role_id           int                    default null,
    is_active         boolean                default true,
    created_timestamp timestamp     not null default current_timestamp,
    updated_timestamp timestamp     not null default current_timestamp
);
create index first_name_idx on tbl_users (first_name);
alter table tbl_users
    add constraint fk_role_id
        foreign key (role_id)
            references vitality.tbl_roles_master (id);