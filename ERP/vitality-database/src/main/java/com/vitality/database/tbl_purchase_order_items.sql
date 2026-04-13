alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_purchase_order_items;
create table vitality.tbl_purchase_order_items
(
    id                bigint primary key not null default nextval('global_sid_seq'),
    po_id             bigint             not null,
    item_desc         text               not null,
    item_qty          numeric            not null default 1,
    estimated_price   numeric                     default 0.0,
    is_active         boolean            not null default true,
    created_timestamp timestamp          not null default current_timestamp,
    updated_timestamp timestamp          not null default current_timestamp
);

alter table vitality.tbl_purchase_order_items
    add constraint fk_po_id foreign key (po_id)
        references vitality.tbl_purchase_order (id)
        ON DELETE RESTRICT;