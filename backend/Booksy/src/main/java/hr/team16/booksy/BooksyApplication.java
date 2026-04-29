package hr.team16.booksy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableMethodSecurity
@SpringBootApplication
public class BooksyApplication {

    public static void main(String[] args) {
        SpringApplication.run(BooksyApplication.class, args);
    }

}
