package com.spring.eventhub;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class EventhubApplicationTests {

	@Value("${app.config.security.pepper}")
	void contextLoads() {
	}

}
