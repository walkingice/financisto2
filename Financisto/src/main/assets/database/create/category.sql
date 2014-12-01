CREATE TABLE category (
    _id integer primary key autoincrement,
    title text not null,
    left integer not null default 0,
    right integer not null default 0,
    last_location_id long not null default 0,
    last_project_id long not null default 0,
    sort_order integer not null default 0,
    type integer not null default 0,
    updated_on TIMESTAMP DEFAULT 0,
    remote_key text);

CREATE INDEX category_left_idx ON category (left);

CREATE INDEX idx_key_cat ON category (remote_key);

INSERT INTO category VALUES(-1,'[Split...]',0,0,0,0,0,0,0,NULL);

INSERT INTO category VALUES(0,'No category',1,2,0,0,1,0,0,NULL);
