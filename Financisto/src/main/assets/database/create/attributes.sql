CREATE TABLE attributes (
    _id integer primary key autoincrement,
    type integer not null default 1,
    name text not null,
    list_values text,
    default_value text,
    updated_on TIMESTAMP DEFAULT 0,
    remote_key text,
    title text);

INSERT INTO attributes VALUES(-1,4,'DELETE_AFTER_EXPIRED',NULL,'true',0,NULL,NULL);

CREATE TABLE category_attribute (
    category_id integer not null,
    attribute_id integer not null);

CREATE INDEX category_attr_idx ON category_attribute (category_id);

CREATE TABLE transaction_attribute (
    transaction_id integer not null,
    attribute_id integer not null,
    value text);

CREATE INDEX transaction_attr_idx ON transaction_attribute (transaction_id);
