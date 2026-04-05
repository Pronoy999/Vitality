alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_prescription cascade;
create table vitality.tbl_prescription
(
    id                     bigint primary key    default nextval('global_sid_seq'),
    prescription_date      date         not null,
    prescription_status    varchar(100) not null,
    prescription_image_url text                  default null,
    safety_score           int                   default null,
    patient_id             varchar(1000),
    referred_by_doctor     varchar(1000)         default null,
    diagnosis              text                  default null,
    status                 varchar(20)           default 'IN_PROCESS',
    is_active              boolean      not null default true,
    created_timestamp      timestamp    not null default current_timestamp,
    updated_timestamp      timestamp    not null default current_timestamp
);
alter table vitality.tbl_prescription
    add constraint fk_patient_id
        foreign key (patient_id)
            references vitality.tbl_patients (guid)
            ON DELETE cascade;