alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_prescription_diagnosis;
create table vitality.tbl_prescription_diagnosis
(
    id                bigint primary key not null default nextval('global_sid_seq'),
    prescription_id   bigint             not null,
    diagnosis         text                        default null,
    medicine_name     varchar(500)       not null default null,
    dosage            varchar(100)                default null,
    unit              numeric                     default null,
    unit_measure      varchar(50)                 default null,
    start_date        date                        default null,
    end_date          date                        default null,
    frequency         varchar(100)                default null,
    is_active         boolean            not null default true,
    created_timestamp timestamp          not null default current_timestamp,
    updated_timestamp timestamp          not null default current_timestamp
);

alter table vitality.tbl_prescription_diagnosis
    add constraint fk_prescription_id foreign key (prescription_id)
        references vitality.tbl_prescription (id)
        ON DELETE CASCADE;