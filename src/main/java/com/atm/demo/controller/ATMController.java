package com.atm.demo.controller;

import com.atm.demo.model.ATM;
import com.atm.demo.model.Customer;
import com.atm.demo.repository.ATMRepository;
import com.atm.demo.repository.CustomerRepository;
import com.atm.demo.service.ATMService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/atm")
public class ATMController {

    private final ATMService atmService;
    private final CustomerRepository customerRepository;
    private final ATMRepository atmRepository;

    public ATMController(ATMService atmService, CustomerRepository customerRepository, ATMRepository atmRepository) {
        this.atmService = atmService;
        this.customerRepository = customerRepository;
        this.atmRepository = atmRepository;
    }

    /**
     * Set the balance of ATM; assuming only single ATM.
     *
     */
    @GetMapping("/machine/{balance}")
    public String setATMBalance(@PathVariable("balance") BigDecimal balance) {
        ATM atm = new ATM(balance);
        this.atmRepository.save(atm);
        return "ATM Balance is £" + atm.getBalance();
    }

    /**
     * Just a debug endpoint to overview current state of the application.
     *
     */
    @GetMapping("/machine/status")
    public Map <String, String> getCurrentState(HttpSession session) {
        String atmDetails =  "ATM Balance is £" + atmRepository
                .findAll()
                .stream()
                .findFirst()
                .map(ATM::getBalance)
                .orElse(new BigDecimal(0));
        List<Customer> customers = customerRepository.findAll();
        Map <String, String> map = new HashMap<>();
        map.put("ATM", atmDetails);
        map.put("CUSTOMER", customers.toString());

        if (session.getAttribute("SESSION_AUTHENTICATED") != null) {
            map.put("SESSION_AUTHENTICATED", session.getAttribute("SESSION_AUTHENTICATED").toString());
        }
        return map;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/customer/add")
    public void createCustomer(@Valid @RequestBody Customer customer) {
        customerRepository.save(customer);
    }

    @PostMapping("/customer/authenticate")
    public String authenticateCustomer(HttpSession session, @RequestBody Map<String, Integer> credentials) {
        Integer accountNumber = credentials.get("account_number");
        Integer pin = credentials.get("pin");
        StringBuilder response = new StringBuilder();

        if (atmService.isAuthenticated(accountNumber, pin)) {
            Optional<Customer> customer = customerRepository.findByAccountNumber(accountNumber);
            customer.ifPresentOrElse((c) -> {
                session.setAttribute("SESSION_AUTHENTICATED", accountNumber);
                session.setMaxInactiveInterval(10);
                response.append(c.getBalance().toString());
            }, () -> {
                response.append("ACCOUNT_ERR");
            });
            return response.toString();
        }

//        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Login to continue.");
        response.append("ACCOUNT_ERR");
        return response.toString();
    }

    @GetMapping("/customer/withdraw/{amount}")
    public String withdrawCash(HttpSession session, @PathVariable("amount") BigDecimal amount) {
        Optional<Customer> customer = atmService.ifAuthenticatedGetCustomer(session);
        try {
            return atmService.withdrawCash(customer.orElseThrow(() -> new Exception("ACCOUNT_ERR")), amount);
        } catch (Exception e) {
            return "ACCOUNT_ERR";
        }
    }

    @GetMapping("/customer/balance")
    public String getBalance(HttpSession session) {
        Optional<Customer> customer = atmService.ifAuthenticatedGetCustomer(session);
        return customer
                .map(c -> c.getBalance().toString())
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Login to continue."))
                .orElse("ACCOUNT_ERR");
    }
}
