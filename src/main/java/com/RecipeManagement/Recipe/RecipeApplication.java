package com.RecipeManagement.Recipe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableMethodSecurity
@SpringBootApplication
public class RecipeApplication {

	@Autowired
	public static void main(String[] args) {
		SpringApplication.run(RecipeApplication.class, args);
	}

}

