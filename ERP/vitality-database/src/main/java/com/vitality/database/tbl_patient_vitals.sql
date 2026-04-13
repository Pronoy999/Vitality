alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_patient_vitals cascade;
create table vitality.tbl_patient_vitals
(
    sid                      BIGINT PRIMARY KEY     DEFAULT nextval('global_sid_seq'),
    patient_id               varchar(1000) not null,
    recorded_at              timestamp     not null default current_timestamp,
    height_in_cms            decimal(10, 2)         default null,
    weight_in_kg             decimal(10, 2)         default null,
    blood_pressure_systolic  decimal(4, 0)          default null,
    blood_pressure_diastolic decimal(4, 0)          default null,
    diabetics_fasting        decimal(6, 2)          default null,
    diabetics_pp             decimal(6, 2)          default null,
    created_timestamp        timestamp     not null default current_timestamp,
    updated_timestamp        timestamp     not null default current_timestamp
);
alter table vitality.tbl_patient_vitals
    add constraint fk_patient_id
        foreign key (patient_id)
            references vitality.tbl_patients (guid);
create index patient_vital_idx on vitality.tbl_patient_vitals (sid, patient_id);