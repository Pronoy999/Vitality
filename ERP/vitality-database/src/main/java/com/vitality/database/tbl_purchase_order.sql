alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_purchase_order;
create table vitality.tbl_purchase_order
(
    id                 bigint primary key default nextval('global_sid_seq'),
    po_number          varchar(200)       default null,
    po_generation_date date               default current_date,
    po_delivery_date   date               default null,
    supplier_id        bigint             default null,
    status             varchar(50)        default 'PO_GENERATED',
    approved_by        varchar(100)       default null,
    approved_on        date               default null,
    is_active          boolean   not null default true,
    created_timestamp  timestamp not null default current_timestamp,
    updated_timestamp  timestamp not null default current_timestamp
);

alter table vitality.tbl_purchase_order
    add constraint fk_spplier_id foreign key (supplier_id)
        references vitality.tbl_supplier(id) on DELETE RESTRICT ;