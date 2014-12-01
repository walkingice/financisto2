CREATE TABLE currency (
    _id integer primary key autoincrement,
    name text not null,
    title text not null,
    symbol text not null,
    is_default integer not null default 0,
    decimals integer not null default 2,
    decimal_separator text,
    group_separator text,
    symbol_format text not null default 'RS',
    updated_on TIMESTAMP DEFAULT 0,
    remote_key text);

CREATE INDEX idx_key_cur ON currency (remote_key);

CREATE TABLE currency_exchange_rate (
    from_currency_id integer not null,
    to_currency_id integer not null,
    rate_date long not null,
    rate float not null,
    updated_on TIMESTAMP DEFAULT 0,
    remote_key text,
    PRIMARY KEY (from_currency_id, to_currency_id, rate_date));

CREATE INDEX idx_key_cur_rate ON currency_exchange_rate (remote_key);