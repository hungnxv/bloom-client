
CREATE TABLE IF NOT EXISTS phone_blacklist (
  id bigserial NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	phone_number varchar(50) NOT NULL,
	added_time timestamp NOT NULL,
	expired_time timestamp NOT NULL,
	CONSTRAINT blacklist PRIMARY KEY (id)
);