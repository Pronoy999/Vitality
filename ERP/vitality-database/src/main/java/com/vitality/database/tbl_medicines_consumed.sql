alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_medicines_consumed cascade;
create table vitality.tbl_medicines_consumed
(
    sid                bigint primary key     default nextval('global_sid_seq'),
    patient_id         varchar(1000) not null,
    medicine_name      TEXT          not null,
    patient_ailment_id bigint                 default null,
    dosage             TEXT                   default null,
    frequency          text                   default null,
    start_date         date                   default null,
    end_date           date                   default null,
    status             varchar(50)            default null,
    prescribed_by      text                   default null,
    is_active          boolean                default true,
    created_timestamp  timestamp     not null default current_timestamp,
    updated_timestamp  timestamp     not null default current_timestamp
);

create index medicines_consumed_idx on vitality.tbl_medicines_consumed (sid, patient_id);
-- Foreign key for Patients.
alter table vitality.tbl_medicines_consumed
    add constraint fk_patient_id
        foreign key (patient_id) references vitality.tbl_patients (guid);
-- Foreign key for Ailments.
alter table vitality.tbl_medicines_consumed
    add constraint fk_ailment foreign key (patient_ailment_id)
        references vitality.tbl_ailments (sid);