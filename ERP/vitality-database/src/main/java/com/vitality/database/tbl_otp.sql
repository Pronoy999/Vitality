ALTER DATABASE vitality SET search_path TO vitality;

DROP TABLE IF EXISTS vitality.tbl_otp;

CREATE TABLE vitality.tbl_otp
(
    sid bigint default nextval('global_sid_seq'),
    phone_number      VARCHAR(15) NOT NULL,
    otp               INTEGER     NOT NULL,
    created_timestamp TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_timestamp TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_tbl_otp PRIMARY KEY (phone_number, otp)
);