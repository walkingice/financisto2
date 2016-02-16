CREATE TABLE account (
    _id integer primary key autoincrement,
    title text not null,
    creation_date long not null,
    currency_id integer not null,
    total_amount integer not null default 0,
    type text not null default 'CASH',
    issuer text,
    number text,
    is_active boolean not null default 1,
    is_include_into_totals boolean not null default 1,
    last_category_id long not null default 0,
    last_account_id long not null default 0,
    total_limit integer not null default 0,
    card_issuer text,
    closing_day integer not null default 0,
    payment_day integer not null default 0,
    note text,
    last_transaction_date long not null default 0,
    updated_on TIMESTAMP DEFAULT 0,
    remote_key text);

CREATE INDEX idx_key_act ON account (remote_key);
