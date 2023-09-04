package com.atm.demo.service;

import com.atm.demo.model.ATM;
import com.atm.demo.model.Customer;
import com.atm.demo.repository.ATMRepository;
import com.atm.demo.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ATMService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ATMRepository atmRepository;

    public String withdrawCash(Customer customer, BigDecimal amount) {
        // Customer customer = customerRepository.findByAccountNumber(accountNumber);
        ATM atm = atmRepository.findAll().get(0); // assuming only 1 atm.

        boolean hasEnoughCashInAccount = customer.getTotalBalance().compareTo(amount) >= 0;
        boolean hasEnoughCashInATM = atm.getBalance().compareTo(amount) >= 0;

        if (hasEnoughCashInATM) {
            if (hasEnoughCashInAccount) {
                customer.setBalance(customer.getBalance().subtract(amount));
                atm.setBalance(atm.getBalance().subtract(amount));

                // Todo: transaction check, & roll back if one fails.
                customerRepository.save(customer);
                atmRepository.save(atm);
                return customer.getBalance().toString();
            }
            return "FUNDS_ERR";
        }
        return "ATM_ERR";
    }

    public Boolean isAuthenticated(Integer accountNumber, Integer pin) {
        Customer customer = customerRepository.findByAccountNumber(accountNumber);
        return customer.getPin().equals(pin);
    }
}
