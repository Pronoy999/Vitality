alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_patient_ailments cascade ;
create table vitality.tbl_patient_ailments
(
    sid               bigint primary key     default nextval('global_sid_seq'),
    patient_id        varchar(1000) not null,
    ailment_type_id   bigint        not null,
    ailment_desc      varchar(200)  not null,
    is_active         boolean                default true,
    created_timestamp timestamp     not null default current_timestamp,
    updated_timestamp timestamp     not null default current_timestamp
);
create index patient_ailment_idx on vitality.tbl_patient_ailments (sid, patient_id, ailment_type_id);
-- Foreign key for Patients.
alter table vitality.tbl_patient_ailments
    add constraint fk_patient_id
        foreign key (patient_id) references tbl_patients (guid);
-- Foreign key for Ailment Type Master.
alter table vitality.tbl_patient_ailments
    add constraint fk_ailment_type
        foreign key (ailment_type_id)
            references tbl_ailment_type_master (sid);