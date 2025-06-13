package ca.bc.gov.educ.api.graduation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@Slf4j
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class, 
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
@EnableCaching
public class EducGraduationApiApplication {

    public static void main(String[] args) {
        log.debug("########Starting API");
        SpringApplication.run(EducGraduationApiApplication.class, args);
        log.debug("########Started API");
    }
}