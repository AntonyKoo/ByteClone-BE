package com.whochucompany.byteclone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ByteCloneApplication {

    public static void main(String[] args) {
        SpringApplication.run(ByteCloneApplication.class, args);
    }

}
