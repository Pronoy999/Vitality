alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_invoice_items;
create table vitality.tbl_invoice_items
(
    id                bigint primary key not null default nextval('global_sid_seq'),
    invoice_id        bigint             not null,
    item_desc         text               not null,
    received_item_qty numeric            not null default 0,
    damaged_item_qty  numeric                     default 0,
    item_price        numeric            not null,
    hsn_code          varchar(500)       not null,
    expiry_date       date                        default null,
    manufactured_date date                        default null,
    batch_number      varchar(500)                default null,
    mrp               numeric                     default null,
    is_active         boolean            not null default true,
    created_timestamp timestamp          not null default current_timestamp,
    updated_timestamp timestamp          not null default current_timestamp
);

alter table vitality.tbl_invoice_items
    add constraint fk_invoice_id foreign key (invoice_id)
        references vitality.tbl_invoice (id)
        on delete restrict;