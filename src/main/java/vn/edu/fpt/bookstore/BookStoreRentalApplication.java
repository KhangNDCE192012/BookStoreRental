package vn.edu.fpt.bookstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookStoreRentalApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookStoreRentalApplication.class, args);
    }
}
