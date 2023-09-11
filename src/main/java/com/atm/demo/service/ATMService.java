package com.atm.demo.service;

import com.atm.demo.model.ATM;
import com.atm.demo.model.Customer;
import com.atm.demo.repository.ATMRepository;
import com.atm.demo.repository.CustomerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ATMService {
    public static final String SESSION_AUTHENTICATED = "SESSION_AUTHENTICATED";

    private final CustomerRepository customerRepository;

    private final ATMRepository atmRepository;

    public ATMService (CustomerRepository customerRepository, ATMRepository atmRepository) {
        this.atmRepository = atmRepository;
        this.customerRepository = customerRepository;
    }

    public String withdrawCash(Customer customer, BigDecimal amount) {
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
        Optional<Customer> customer = customerRepository.findByAccountNumber(accountNumber);
        return customer.map(c -> c.getPin().equals(pin)).orElse(false);
    }

    public Optional<Customer> ifAuthenticatedGetCustomer(HttpSession session) {
        var authenticatedCustomer = session.getAttribute(SESSION_AUTHENTICATED);
        if (authenticatedCustomer != null) {
            return customerRepository.findByAccountNumber((Integer) session.getAttribute(SESSION_AUTHENTICATED));
        }
        return Optional.empty();
    }
}
