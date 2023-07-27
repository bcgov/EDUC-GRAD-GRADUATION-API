package ca.bc.gov.educ.api.graduation.config;

import ca.bc.gov.educ.api.graduation.util.GradLocalDateDeserializer;
import ca.bc.gov.educ.api.graduation.util.GradLocalDateSerializer;
import ca.bc.gov.educ.api.graduation.util.GradLocalDateTimeDeserializer;
import ca.bc.gov.educ.api.graduation.util.GradLocalDateTimeSerializer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@IntegrationComponentScan
@EnableIntegration
public class EducGraduationApplicationConfig {

    @Bean
    public ModelMapper modelMapper() {

        ModelMapper modelMapper = new ModelMapper();
        return modelMapper;
    }

    @Bean
    @Primary
    ObjectMapper jacksonObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDate.class, new GradLocalDateSerializer());
        simpleModule.addSerializer(LocalDateTime.class, new GradLocalDateTimeSerializer());
        simpleModule.addDeserializer(LocalDate.class, new GradLocalDateDeserializer());
        simpleModule.addDeserializer(LocalDateTime.class, new GradLocalDateTimeDeserializer());
        mapper.findAndRegisterModules();
        mapper.registerModule(simpleModule);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        return mapper;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

}
