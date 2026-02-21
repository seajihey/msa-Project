package msa.authservice.security;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHashPrinter implements CommandLineRunner {
	@Override
	public void run(String... args) {
		System.out.println(new BCryptPasswordEncoder().encode("pass"));
	}
}
