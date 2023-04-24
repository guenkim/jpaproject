package spring_data_jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;


/*************************************
 * Auditing 사용하려면 아래 어노테이션 설정 필요
 * ***********************************/
@EnableJpaAuditing //스프링 부트 설정 클래스에 적용해야함
@SpringBootApplication
public class SpringDataJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringDataJpaApplication.class, args);
	}

	/**
	 * * Auditing
	 *  - SPRING DATA JPA 사용
	 *
		등록자, 수정자를 처리해주는 AuditorAware 스프링 빈 등록
	 **/
	@Bean
	public AuditorAware<String> auditorProvider() {
		return new AuditorAware<String>() {
			@Override
			public Optional<String> getCurrentAuditor() {

				//여기서는 ID를 UUID로 처리
				//실제에서는 SESSION에서 ID를 뽑아서 넣어준다.
				return Optional.of(UUID.randomUUID().toString());
			}
		};
	}

}
