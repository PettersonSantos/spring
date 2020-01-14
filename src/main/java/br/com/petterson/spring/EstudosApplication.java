package br.com.petterson.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"br.com.petterson.spring"})
public class EstudosApplication {

    public static void main(String[] args) {
        SpringApplication.run(EstudosApplication.class, args);
    }

}
