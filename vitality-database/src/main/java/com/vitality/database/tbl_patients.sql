alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_patients cascade;
create table vitality.tbl_patients
(
    guid                         varchar(1000) not null primary key,
    abha_id                      varchar(1000)          default null,
    first_name                   varchar(200)  not null,
    last_name                    varchar(500)  not null,
    age                          decimal(3, 0)          default null,
    gender                       varchar(20)    not null,
    phone_number                 varchar(15)         default null,
    email_id                     varchar(1000)          default null,
    height_in_cms                decimal(6, 2)          default null,
    weight_in_kgs                decimal(6, 2)          default null,
    blood_pressure               varchar(20)            default null,
    ailment_history              text                   default null,
    has_heath_insurance          boolean                default false not null,
    additional_diagnosis         text                   default null,
    medicines_consumed           text                   default null,
    additional_services_required text                   default null,
    created                      timestamp     not null default current_timestamp,
    updated                      timestamp     not null default current_timestamp
);
create index abha_first_name_idx on tbl_patients (abha_id, first_name);