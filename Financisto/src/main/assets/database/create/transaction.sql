CREATE TABLE transactions (
    _id integer primary key autoincrement,
    from_account_id long not null,
    to_account_id long not null default 0,
    category_id long not null default 0,
    project_id long not null default 0,
    note text,
    from_amount integer not null default 0,
    to_amount integer not null default 0,
    datetime long not null,
    payee text,
    is_template integer not null default 0,
    template_name text,
    recurrence text,
    notification_options text,
    status text not null default 'UR',
    attached_picture text,
    is_ccard_payment integer not null default 0,
    last_recurrence long not null default 0,
    payee_id long,
    parent_id long not null default 0,
    updated_on TIMESTAMP DEFAULT 0,
    remote_key text,
    original_currency_id long not null default 0,
    original_from_amount long not null default 0,
    blob_key text
);

CREATE INDEX transaction_from_act_idx ON transactions (from_account_id);
CREATE INDEX transaction_to_act_idx ON transactions (to_account_id);
CREATE INDEX transaction_dt_idx ON transactions (datetime desc);
CREATE INDEX idx_is_template ON transactions(is_template);
CREATE INDEX transaction_pid_idx ON transactions (parent_id);
