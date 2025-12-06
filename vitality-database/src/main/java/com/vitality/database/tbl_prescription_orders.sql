alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_prescription_orders cascade;
create table vitality.tbl_prescription_orders
(
    id                     int primary key not null,
    order_date             date            not null,
    order_status           varchar(100)    not null,
    prescription_image_url text                     default null,
    safety_score           int                      default null,
    patient_id             varchar(1000)   not null,
    doctor_id              varchar(1000)            default null,
    is_active              boolean         not null default true,
    created_timestamp      timestamp       not null default current_timestamp,
    updated_timestamp      timestamp       not null default current_timestamp
);
alter table vitality.tbl_prescription_orders
    add constraint fk_patient_id
        foreign key (patient_id)
            references vitality.tbl_patients (guid)
            ON DELETE cascade;