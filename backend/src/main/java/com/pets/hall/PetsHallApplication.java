package com.pets.hall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pets.hall.mapper")
public class PetsHallApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetsHallApplication.class, args);
    }
}
