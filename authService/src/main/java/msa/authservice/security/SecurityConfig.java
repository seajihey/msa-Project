package msa.authservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/login", "/auth/login").permitAll()
				// actuator health도 열어두면 편함
				.requestMatchers("/actuator/health", "/actuator/info").permitAll()
				// 나머지는 일단 다 허용(또는 authenticated로 바꿔도 됨)
				.anyRequest().permitAll()
			)
			// 기본 로그인폼/베이직 인증 끄기(불필요한 401 방지)
			.httpBasic(Customizer.withDefaults())
			.formLogin(form -> form.disable());

		return http.build();
	}
}
