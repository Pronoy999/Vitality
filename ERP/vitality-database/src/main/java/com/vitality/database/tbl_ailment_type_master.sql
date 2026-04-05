alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_ailment_type_master;
create table vitality.tbl_ailment_type_master
(
    sid               bigint primary key    default nextval('global_sid_seq'),
    code              varchar(50)           default null,
    type              varchar(200) not null,
    created_timestamp timestamp    not null default current_timestamp,
    updated_timestamp timestamp    not null default current_timestamp
);