CREATE TABLE locations (
    _id integer primary key autoincrement,
    name text not null,
    datetime long not null,
    provider text,
    accuracy float,
    latitude double,
    longitude double,
    is_payee integer not null default 0,
    resolved_address text,
    count integer not null default 0,
    updated_on TIMESTAMP DEFAULT 0,
    remote_key text);

CREATE INDEX idx_key_loc ON locations (remote_key);

INSERT INTO locations VALUES(0,'Current location',0,'?','?',0.0,0.0,0,'?',0,0,NULL);
