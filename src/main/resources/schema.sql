create sequence atm_seq start with 1 increment by 50;
create table atm (balance numeric(38,2), id bigint not null, primary key (id));
create table customer (account_number integer not null, balance numeric(38,2), overdraft_facility numeric(38,2), pin integer, primary key (account_number));