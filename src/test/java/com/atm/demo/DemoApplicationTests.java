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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DemoApplicationTests {

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private ATMRepository atmRepository;

	@Autowired
	private ATMService atmService;

	private Customer existingCustomer;

	void contextLoads() {
		atmRepository.save(new ATM(BigDecimal.valueOf(8000)));
		customerRepository.save(new Customer(
				12345678,
				1234,
				BigDecimal.valueOf(500),
				BigDecimal.valueOf(100)
		));
		customerRepository.findByAccountNumber(12345678).ifPresent(c -> { this.existingCustomer = c; });
	}

	@Test
	public void nonExistingCustomer() {
		System.out.println("nonExistingCustomer: ");
		this.contextLoads();
		Optional<Customer> customer1 = customerRepository.findByAccountNumber(12345678);
		System.out.println("Existing customer response: " + customer1.toString());
		Optional<Customer> customer2 = customerRepository.findByAccountNumber(12345677);
		System.out.println("Non-Existing customer response: " + customer2.isPresent());
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
		System.out.println("Before Customer: " + existingCustomer.toString());
		String response = atmService.withdrawCash(existingCustomer, BigDecimal.valueOf(400));
		System.out.println("response: " + response);
		assertThat(response).isEqualTo("100.00");
		System.out.println("After Customer: " + existingCustomer.toString());
		ATM atm = atmRepository.findAll().get(0);
		assertThat(atm.getBalance()).isEqualTo("7600.00");
	}

	@Test
	public void outOfCashOnAtm() {
		this.contextLoads();
		System.out.println("Before Customer: " + existingCustomer.toString());
		String response = atmService.withdrawCash(existingCustomer, BigDecimal.valueOf(8001));
		System.out.println("response: " + response);
		assertThat(response).isEqualTo("ATM_ERR");
		System.out.println("After Customer: " + existingCustomer.toString());
	}

	@Test
	public void outOfCashOnAccount() {
		this.contextLoads();
		System.out.println("Before Customer: " + existingCustomer.toString());
		String response = atmService.withdrawCash(existingCustomer, BigDecimal.valueOf(601));
		System.out.println("response: " + response);
		assertThat(response).isEqualTo("FUNDS_ERR");
		System.out.println("After Customer: " + existingCustomer.toString());
	}

	@Test
	public void multipleWithdraw() {
		this.contextLoads();
		System.out.println("Before Customer: " + existingCustomer.toString());
		String response = atmService.withdrawCash(existingCustomer, BigDecimal.valueOf(599));
		System.out.println("response: " + response);
		System.out.println("After Customer: " + existingCustomer.toString());
		assertThat(response).isEqualTo("-99.00");
		String response2 = atmService.withdrawCash(existingCustomer, BigDecimal.valueOf(1));
		System.out.println("response2: " + response2);
		System.out.println("After 2 Customer: " + existingCustomer.toString());
		assertThat(response2).isEqualTo("-100.00");
		String response3 = atmService.withdrawCash(existingCustomer, BigDecimal.valueOf(1));
		System.out.println("response3: " + response3);
		System.out.println("After 3 Customer: " + existingCustomer.toString());
		assertThat(response3).isEqualTo("FUNDS_ERR");
	}

	@Test
	public void findCustomerByAccountNumber() {
		this.contextLoads();
		System.out.println(existingCustomer.toString());
		System.out.println("Taking out £20.");
		existingCustomer.setBalance(existingCustomer.getBalance().subtract(BigDecimal.valueOf(20)));
		customerRepository.save(existingCustomer);

		System.out.println(existingCustomer.toString());
		assertThat(existingCustomer.getBalance().toString()).isEqualTo("480.00");

		assertThat(existingCustomer).isInstanceOf(Customer.class);

		System.out.println("From DB: " + existingCustomer.toString());

		BigDecimal amountToSubtract = BigDecimal.valueOf(600);
		BigDecimal totalBalance = existingCustomer.getBalance().add(existingCustomer.getOverdraftFacility());
		System.out.println("Total balance: " + totalBalance);
		System.out.println("amountToSubtract: " + amountToSubtract);

		existingCustomer.setBalance(totalBalance.subtract(amountToSubtract));
		System.out.println("After subtract: " + existingCustomer.toString());

		BigDecimal newTotalBalance = existingCustomer.getBalance().add(existingCustomer.getOverdraftFacility());
		System.out.println("newTotalBalance: " + newTotalBalance);



		BigDecimal afterWithdraw = existingCustomer.getBalance().subtract(BigDecimal.valueOf(-600));
		System.out.println("After withdraw: " + afterWithdraw);
	}

}
