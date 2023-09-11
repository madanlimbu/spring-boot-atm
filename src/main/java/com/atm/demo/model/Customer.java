package com.atm.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.math.BigDecimal;

@Entity
public class Customer {
    @Id
    private Integer accountNumber;
    private Integer pin;
    private BigDecimal balance;
    private BigDecimal overdraftFacility;

    protected Customer() {}

    public Customer(Integer accountNumber, Integer pin, BigDecimal balance, BigDecimal overdraftFacility) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = balance;
        this.overdraftFacility = overdraftFacility;
    }

    @Override
    public String toString() {
        return "Customer {" +
                ", accountNumber = " + accountNumber +
                ",  pin = " + pin +
                ",  balance = £" + balance +
                ",  overdraftFacility = £" + overdraftFacility +
                '}';
    }

    public Integer getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(Integer accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Integer getPin() {
        return pin;
    }

    public void setPin(Integer pin) {
        this.pin = pin;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getTotalBalance() {
        return this.balance.add(this.overdraftFacility);
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getOverdraftFacility() {
        return overdraftFacility;
    }

    public void setOverdraftFacility(BigDecimal overdraftFacility) {
        this.overdraftFacility = overdraftFacility;
    }
}
