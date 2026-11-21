// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package vysion.profile;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import secop.ppm1.ProfileApplication;

@SpringBootTest(classes = ProfileApplication.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "spring.jpa.show-sql=false",
    "spring.security.user.name=test",
    "spring.security.user.password=test",
    "logging.level.org.springframework.security=INFO",
    "logging.level.org.hibernate.SQL=INFO"
})
class ProfileApplicationTests {

	@Test
	void contextLoads() {
	}

}
