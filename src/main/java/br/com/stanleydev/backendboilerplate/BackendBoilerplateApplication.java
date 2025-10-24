package br.com.stanleydev.backendboilerplate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BackendBoilerplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendBoilerplateApplication.class, args);
    }

}
