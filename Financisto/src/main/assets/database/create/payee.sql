CREATE TABLE payee (
    _id integer primary key autoincrement,
    title text,
    last_category_id long not null default 0,
    is_active boolean not null default 1,
    updated_on TIMESTAMP DEFAULT 0,
    remote_key text
);

CREATE INDEX idx_key_payee ON payee (remote_key);
