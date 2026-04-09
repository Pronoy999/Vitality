alter database vitality set search_path to vitality;
drop table if exists vitality.tbl_patients cascade;
create table vitality.tbl_patients
(
    id                           bigint primary key not null default nextval('global_sid_seq'),
    user_id                      varchar(1000)               default null,
    abha_id                      varchar(1000)               default null,
    first_name                   varchar(200)       not null,
    last_name                    varchar(500)       not null,
    age                          decimal(3, 0)               default null,
    gender                       varchar(20)        not null,
    phone_number                 varchar(15)                 default null,
    email_id                     varchar(1000)               default null,
    height_in_cms                decimal(6, 2)               default null,
    weight_in_kgs                decimal(6, 2)               default null,
    blood_pressure               varchar(20)                 default null,
    ailment_history              text                        default null,
    health_parameters            text                        default null,
    has_heath_insurance          boolean                     default false not null,
    additional_diagnosis         text                        default null,
    medicines_consumed           text                        default null,
    additional_services_required text                        default null,
    is_active                     boolean                     default true not null,
    created                      timestamp          not null default current_timestamp,
    updated                      timestamp          not null default current_timestamp
);
create index abha_first_name_idx on tbl_patients (abha_id, first_name);

alter table tbl_patients
    add constraint fk_user_id foreign key (user_id)
        references tbl_users (guid);