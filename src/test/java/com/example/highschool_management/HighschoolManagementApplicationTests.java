package com.example.highschool_management;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.example.highschool_management.dao.InvalidatedTokenDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "app.seed.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:startup;MODE=MySQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password="
})
class HighschoolManagementApplicationTests {

	@Test
	void initializesTokenRevocationStore(@Autowired InvalidatedTokenDao tokens) {
		assertFalse(tokens.existsById("fresh-token"));
	}

}
