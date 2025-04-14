package com.project.webBuilder;

import com.project.webBuilder.common.util.Screenshot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.io.IOException;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableJpaAuditing
public class WebBuilderApplication {

	public static void main(String[] args){
		SpringApplication.run(WebBuilderApplication.class, args);
	}

}
