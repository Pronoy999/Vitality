alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_order;
create table vitality.tbl_order
(
    id                bigint primary key not null default nextval('global_sid_seq'),
    patient_id        bigint             not null,
    order_date        date               not null,
    total_items       numeric            not null,
    order_status      varchar(20)        not null,
    total_item_price  numeric            not null,
    total_discount    numeric                     default 0.0,
    total_tax_amount  numeric                     default 0.0,
    platform_fee      numeric                     default 0.0,
    delivery_fee      numeric                     default 0.0,
    round_off_amount  numeric                     default 0.0,
    total_price       numeric            not null,
    is_active         boolean                     default true not null,
    created_timestamp timestamp          not null default current_timestamp,
    updated_timestamp timestamp          not null default current_timestamp
);
alter table vitality.tbl_order
    add constraint fk_patient_id foreign key (patient_id)
        references vitality.tbl_patients (id) on delete set null;