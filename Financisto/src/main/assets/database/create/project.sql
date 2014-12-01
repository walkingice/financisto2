CREATE TABLE project (
    _id integer primary key autoincrement,
    title text,
    is_active boolean not null default 1,
    updated_on TIMESTAMP DEFAULT 0,
    remote_key text);

CREATE INDEX idx_key_pro ON project (remote_key);

INSERT INTO project VALUES(0,'No project',1,0,NULL);
