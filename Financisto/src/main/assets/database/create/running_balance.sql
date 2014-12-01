CREATE TABLE running_balance (
    account_id integer not null,
    transaction_id integer not null,
    datetime long not null,
    balance integer not null,
    PRIMARY KEY (account_id, transaction_id)
);

CREATE INDEX running_balance_act_idx ON running_balance (account_id);
CREATE INDEX running_balance_txn_idx ON running_balance (transaction_id);
CREATE INDEX running_balance_dt_idx ON running_balance (datetime);