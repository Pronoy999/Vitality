alter database vitality set search_path to vitality;
create sequence vitality.global_sid_seq
    start with 10000
    increment by 1
    no cycle ;