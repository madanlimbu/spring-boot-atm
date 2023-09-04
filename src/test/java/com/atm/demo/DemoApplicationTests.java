package com.atm.demo;

import com.atm.demo.model.ATM;
import com.atm.demo.model.Customer;
import com.atm.demo.repository.ATMRepository;
import com.atm.demo.repository.CustomerRepository;
import com.atm.demo.service.ATMService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DemoApplicationTests {

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private ATMRepository atmRepository;

	@Autowired
	private ATMService atmService;

	void contextLoads() {
		atmRepository.save(new ATM(BigDecimal.valueOf(8000)));
		customerRepository.save(new Customer(
				12345678,
				1234,
				BigDecimal.valueOf(500),
				BigDecimal.valueOf(100)
		));
	}

	@Test
	public void checkAuth() {
		this.contextLoads();
		Boolean response = atmService.isAuthenticated(12345678, 1234);
		assertThat(response).isEqualTo(true);
		Boolean response2 = atmService.isAuthenticated(12345678, 1233);
		assertThat(response2).isEqualTo(false);
	}

	@Test
	public void validWithdraw() {
		this.contextLoads();
		Customer customer = customerRepository.findByAccountNumber(12345678);
		System.out.println("Before Customer: " + customer.toString());
		String response = atmService.withdrawCash(customer, BigDecimal.valueOf(400));
		System.out.println("response: " + response);
		assertThat(response).isEqualTo("100.00");
		System.out.println("After Customer: " + customer.toString());
		ATM atm = atmRepository.findAll().get(0);
		assertThat(atm.getBalance()).isEqualTo("7600.00");
	}

	@Test
	public void outOfCashOnAtm() {
		this.contextLoads();
		Customer customer = customerRepository.findByAccountNumber(12345678);
		System.out.println("Before Customer: " + customer.toString());
		String response = atmService.withdrawCash(customer, BigDecimal.valueOf(8001));
		System.out.println("response: " + response);
		assertThat(response).isEqualTo("ATM_ERR");
		System.out.println("After Customer: " + customer.toString());
	}

	@Test
	public void outOfCashOnAccount() {
		this.contextLoads();
		Customer customer = customerRepository.findByAccountNumber(12345678);
		System.out.println("Before Customer: " + customer.toString());
		String response = atmService.withdrawCash(customer, BigDecimal.valueOf(601));
		System.out.println("response: " + response);
		assertThat(response).isEqualTo("FUNDS_ERR");
		System.out.println("After Customer: " + customer.toString());
	}

	@Test
	public void multipleWithdraw() {
		this.contextLoads();
		Customer customer = customerRepository.findByAccountNumber(12345678);
		System.out.println("Before Customer: " + customer.toString());
		String response = atmService.withdrawCash(customer, BigDecimal.valueOf(599));
		System.out.println("response: " + response);
		System.out.println("After Customer: " + customer.toString());
		assertThat(response).isEqualTo("-99.00");
		String response2 = atmService.withdrawCash(customer, BigDecimal.valueOf(1));
		System.out.println("response2: " + response2);
		System.out.println("After 2 Customer: " + customer.toString());
		assertThat(response2).isEqualTo("-100.00");
		String response3 = atmService.withdrawCash(customer, BigDecimal.valueOf(1));
		System.out.println("response3: " + response3);
		System.out.println("After 3 Customer: " + customer.toString());
		assertThat(response3).isEqualTo("FUNDS_ERR");
	}

	@Test
	public void findCustomerByAccountNumber() {
		customerRepository.save(new Customer(
				12345678,
				1234,
				BigDecimal.valueOf(500),
				BigDecimal.valueOf(100)
		));
		Customer c = customerRepository.findByAccountNumber(12345678);
		System.out.println(c.toString());
		System.out.println("Taking out £20.");
		c.setBalance(c.getBalance().subtract(BigDecimal.valueOf(20)));
		customerRepository.save(c);

		System.out.println(c.toString());
		assertThat(c.getBalance().toString()).isEqualTo("480.00");

		Customer fromDb = customerRepository.findByAccountNumber(12345678);
		assertThat(fromDb).isInstanceOf(Customer.class);

		System.out.println("From DB: " + fromDb.toString());

		BigDecimal amountToSubtract = BigDecimal.valueOf(600);
		BigDecimal totalBalance = c.getBalance().add(c.getOverdraftFacility());
		System.out.println("Total balance: " + totalBalance);
		System.out.println("amountToSubtract: " + amountToSubtract);

		c.setBalance(totalBalance.subtract(amountToSubtract));
		System.out.println("After subtract: " + c.toString());

		BigDecimal newTotalBalance = c.getBalance().add(c.getOverdraftFacility());
		System.out.println("newTotalBalance: " + newTotalBalance);


//
//		BigDecimal afterWithdraw = c.getBalance().subtract(BigDecimal.valueOf(-600));
//		System.out.println("After withdraw: " + afterWithdraw);
	}

}
