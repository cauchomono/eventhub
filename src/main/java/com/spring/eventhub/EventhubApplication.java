package com.spring.eventhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

import com.spring.eventhub.config.AppSecurityProperties;


@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@EnableConfigurationProperties(AppSecurityProperties.class)
public class EventhubApplication {
    
	public static void main(String[] args) {
		SpringApplication.run(EventhubApplication.class, args);
	
		
	}

}
