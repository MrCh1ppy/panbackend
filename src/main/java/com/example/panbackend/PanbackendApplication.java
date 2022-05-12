package com.example.panbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.panbackend.dao.mapper")
public class PanbackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PanbackendApplication.class, args);
	}

}
