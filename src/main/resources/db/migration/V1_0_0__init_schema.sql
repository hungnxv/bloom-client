CREATE TABLE IF NOT EXISTS blacklist (
	id bigserial NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	phone_number varchar(50) NOT NULL,
	added_at timestamp NOT NULL,
	removed_at timestamp NOT NULL,
	CONSTRAINT blacklist PRIMARY KEY (id)
);