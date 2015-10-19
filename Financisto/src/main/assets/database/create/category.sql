CREATE TABLE category (
    _id integer primary key autoincrement,
    title text not null,
    left integer not null default 0,
    right integer not null default 0,
    type integer not null default 0,
    level integer not null default 0,
    last_project_id long not null default 0,
    updated_on TIMESTAMP DEFAULT 0,
    remote_key text);

CREATE INDEX category_left_idx ON category (left);

CREATE INDEX idx_key_cat ON category (remote_key);

INSERT INTO category(_id,title,left,right) VALUES(-1,-99000,-99000,'[Split...]');

INSERT INTO category(_id,title,left,right) VALUES(0,-90000,-90000,'No category');
