alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_patient_vitals cascade;
create table vitality.tbl_inventory
(
    sid                bigint primary key not null default nextval('global_sid_seq'),
    item_desc          text               not null,
    quantity_available integer            not null,
    quantity_reserved  integer                     default 0,
    batch_number       varchar(1000)               default null,
    manufacturing_date date                        default null,
    expiry_date        date                        default null,
    supplier_id        bigint             not null,
    invoice_id         bigint                      default null,
    purchase_price     numeric(10, 2)     not null,
    selling_price      numeric(10, 2)     not null,
    mrp                numeric(10, 2)     not null,
    is_active          boolean                     default true not null,
    created_timestamp  timestamp          not null default current_timestamp,
    updated_timestamp  timestamp          not null default current_timestamp
);

alter table vitality.tbl_inventory
    add constraint fk_supplier
        foreign key (supplier_id) references vitality.tbl_supplier (id);

alter table vitality.tbl_inventory
    add constraint fk_invoice
        foreign key (invoice_id) references vitality.tbl_invoice (id);