alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_invoice;
create table vitality.tbl_invoice
(
    id                bigint primary key not null default nextval('global_sid_seq'),
    po_id             bigint                      default null,
    invoice_id        varchar(500)                default null,
    supplier_id       bigint                      default null,
    invoice_date      date                        default current_date,
    received_date     date                        default null,
    status            varchar(50)                 default 'INVOICE_RAISED',
    item_total_price  numeric                     default 0.0,
    total_discount    numeric                     default 0.0,
    logistic_amount   numeric                     default 0.0,
    insurance_amount  numeric                     default 0.0,
    round_off_amount  numeric                     default 0.0,
    tax_amt           numeric                     default 0.0,
    total_price       numeric                     default 0.0,
    is_active         boolean            not null default true,
    created_timestamp timestamp          not null default current_timestamp,
    updated_timestamp timestamp          not null default current_timestamp
);

alter table vitality.tbl_invoice
    add constraint fk_po_invoice_id foreign key (po_id)
        references vitality.tbl_purchase_order (id)
        on delete RESTRICT;

alter table vitality.tbl_invoice
    add constraint fk_supplier_id foreign key (supplier_id)
        references vitality.tbl_supplier (id) on delete set null;