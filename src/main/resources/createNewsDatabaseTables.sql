CREATE TABLE IF NOT EXISTS news (
id BIGSERIAL,
create_date_time TIMESTAMPTZ NOT NULL,
title CHARACTER VARYING(100) NOT NULL,
text_data TEXT NOT NULL,
author CHARACTER VARYING(40) NOT NULL,

PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS comment (
id BIGSERIAL,
create_date_time TIMESTAMPTZ NOT NULL,
text_data VARCHAR(800) NOT NULL,
user_name CHARACTER VARYING(35) NOT NULL,
news_id BIGSERIAL NOT NULL,

PRIMARY KEY(id),

CONSTRAINT fk_news
FOREIGN KEY (news_id)
REFERENCES news (id)
ON DELETE NO ACTION ON UPDATE NO ACTION
);
