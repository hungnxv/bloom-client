
CREATE SEQUENCE IF NOT EXISTS phone_blacklist_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
	CACHE 1
	NO CYCLE;

CREATE TABLE IF NOT EXISTS phone_blacklist (
  id int8 NOT NULL DEFAULT nextval('phone_blacklist_id_seq'::regclass),
	deleted bool NOT NULL DEFAULT false,
	phone_number varchar(50) NOT NULL,
	added_at timestamp NOT NULL,
	removed_at timestamp NOT NULL,
	CONSTRAINT blacklist PRIMARY KEY (id)
);