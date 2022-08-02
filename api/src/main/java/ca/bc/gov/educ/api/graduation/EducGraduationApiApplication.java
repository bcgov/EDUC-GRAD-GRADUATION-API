package ca.bc.gov.educ.api.graduation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class, 
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
@EnableCaching
public class EducGraduationApiApplication {

    private static Logger logger = LoggerFactory.getLogger(EducGraduationApiApplication.class);

    public static void main(String[] args) {
        logger.debug("########Starting API");
        SpringApplication.run(EducGraduationApiApplication.class, args);
        logger.debug("########Started API");
    }
}