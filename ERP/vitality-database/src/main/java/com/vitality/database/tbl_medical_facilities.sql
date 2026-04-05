alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_medical_facilities cascade;
create table vitality.tbl_medical_facilities
(
    id                 int primary key not null,
    facility_name      varchar(500)    not null,
    facility_speciality varchar(500)    not null,
    commission_credit  decimal(10, 2)           default null,
    is_active          boolean         not null default true,
    created_timestamp  timestamp       not null default current_timestamp,
    updated_timestamp  timestamp       not null default current_timestamp
);