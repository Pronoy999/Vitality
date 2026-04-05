
alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_roles_master;
create table vitality.tbl_roles_master
(
    id                int primary key not null,
    role_name         varchar(20)     not null,
    is_active         boolean         not null default true,
    created_timestamp timestamp       not null default current_timestamp,
    updated_timestamp timestamp       not null default current_timestamp
);