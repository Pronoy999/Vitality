alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_order_items;
create table vitality.tbl_order_items
(
    id                bigint primary key not null default nextval('global_sid_seq'),
    order_id          bigint             not null,
    item_id           bigint             not null,
    quantity          numeric            not null,
    item_price        numeric            not null,
    item_discount     numeric            not null,
    cgst_percentage   numeric                     default 0.0,
    cgst_amount       numeric                     default 0.0,
    sgst_percentage   numeric                     default 0.0,
    sgst_amount       numeric                     default 0.0,
    igst_percentage   numeric                     default 0.0,
    igst_amount       numeric                     default 0.0,
    total_tax_amount  numeric                     default 0.0,
    item_total_price  numeric            not null,
    is_active         boolean                     default true not null,
    created_timestamp timestamp          not null default current_timestamp,
    updated_timestamp timestamp          not null default current_timestamp
);
alter table vitality.tbl_order_items
    add constraint fk_order_id foreign key (order_id)
        references vitality.tbl_order (id) on delete restrict;