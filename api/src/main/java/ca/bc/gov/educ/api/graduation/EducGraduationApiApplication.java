package ca.bc.gov.educ.api.graduation;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class EducGraduationApiApplication {

	private static Logger logger = LoggerFactory.getLogger(EducGraduationApiApplication.class);

	@Value("${spring.security.user.name}")
	private String uName;
	    
	@Value("${spring.security.user.password}")
	private String pass;
	
	
	public static void main(String[] args) {
		logger.debug("########Starting API");
		SpringApplication.run(EducGraduationApiApplication.class, args);
		logger.debug("########Started API");
	}

	@Bean
	public ModelMapper modelMapper() {

		ModelMapper modelMapper = new ModelMapper();
		return modelMapper;
	}	
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.basicAuthentication(uName, pass).build();
	}
}