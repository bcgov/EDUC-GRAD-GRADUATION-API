package ca.bc.gov.educ.api.graduation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.TimeZone;

import static ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants.DATETIME_FORMAT;

@Configuration
public class EducGraduationApplicationConfig {

    @Bean
    public ModelMapper modelMapper() {

        ModelMapper modelMapper = new ModelMapper();
        return modelMapper;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
        TimeZone defaultTimezone = TimeZone.getDefault();
        String timeZoneId = Optional.ofNullable(System.getenv("TZ")).orElse(defaultTimezone.getID());
        LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
        return jacksonObjectMapperBuilder ->
                jacksonObjectMapperBuilder.serializers(localDateTimeSerializer).timeZone(TimeZone.getTimeZone(timeZoneId));
    }

}
