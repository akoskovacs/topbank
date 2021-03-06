CREATE TABLE Office (
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1 INCREMENT BY 1) PRIMARY KEY,
  city VARCHAR(40) NOT NULL,
  address VARCHAR(40) NOT NULL,
  postal_code INTEGER NOT NULL
);

CREATE TABLE Customer (
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1000 INCREMENT BY 1) PRIMARY KEY,
  name VARCHAR(40) NOT NULL,
  private_id VARCHAR(40) NOT NULL,
  city VARCHAR(40) NOT NULL,
  address VARCHAR(40) NOT NULL,
  postal_code INTEGER NOT NULL,
  created_at TIMESTAMP NOT NULL,
  is_admin BOOLEAN NOT NULL,
  password VARCHAR(512) NOT NULL,
  office_id INTEGER NOT NULL,
  CONSTRAINT OFCUFK FOREIGN KEY(office_id) REFERENCES Office (id)
);

CREATE TABLE Account (
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1000 INCREMENT BY 1) PRIMARY KEY, 
  fee INTEGER NOT NULL,
  credit_loan INTEGER NOT NULL,
  credit_interest REAL NOT NULL,
  created_at TIMESTAMP NOT NULL,
  customer_id INTEGER NOT NULL,
  CONSTRAINT CUACFK FOREIGN KEY(customer_id) REFERENCES Customer (id)
);

CREATE TABLE Card (
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1000 INCREMENT BY 1) PRIMARY KEY,
  cardtype INTEGER NOT NULL,
  fee INTEGER NOT NULL,
  pin INTEGER NOT NULL,
  ccv2 INTEGER NOT NULL,
  created_at TIMESTAMP NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  account_id INTEGER NOT NULL,
  CONSTRAINT ACCAFK FOREIGN KEY(account_id) REFERENCES Account (id)
);

CREATE TABLE Transaction (
  id INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1 INCREMENT BY 1) PRIMARY KEY,
  amount INTEGER NOT NULL,
  tr_statement VARCHAR(120) NULL,
  tr_comment VARCHAR(120) NULL,
  payer_account_id INTEGER NULL,
  payee_account_id INTEGER NULL,
  created_at TIMESTAMP NOT NULL
  /*CONSTRAINT ACERTAFK FOREIGN KEY(payer_account_id) REFERENCES Account (id),
  CONSTRAINT ACEETAFK FOREIGN KEY(payee_account_id) REFERENCES Account (id)*/
);
