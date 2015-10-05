CREATE TABLE category (
    _id integer primary key autoincrement,
    parent_id integer not null default 0,
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

INSERT INTO category(_id, parent_id, title) VALUES(-1,0,'[Split...]');

INSERT INTO category(_id, parent_id, title) VALUES(0,0,'No category');
