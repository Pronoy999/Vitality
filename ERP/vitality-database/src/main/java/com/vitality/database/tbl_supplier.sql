alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_supplier;
create table vitality.tbl_supplier
(
    id                        bigint primary key     default nextval('global_sid_seq'),
    supplier_name             varchar(1000) not null,
    poc_name                  varchar(1000) not null,
    poc_contact               varchar(500)  not null,
    estimate_delivery_in_days numeric                default null,
    is_active                 boolean       not null default true,
    created_timestamp         timestamp     not null default current_timestamp,
    updated_timestamp         timestamp     not null default current_timestamp
);
