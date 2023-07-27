package ca.bc.gov.educ.api.graduation.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GradLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    private static final Logger logger = LoggerFactory.getLogger(GradLocalDateTimeDeserializer.class);

    @Override
    public void serialize(LocalDateTime localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        String fieldName = jsonGenerator.getOutputContext().getCurrentName();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        if(localDate != null) {
            String value = localDate.format(formatter);
            logger.debug("Serialize LocalDateTime of {} to value {}", fieldName, value);
            jsonGenerator.writeString(value);
        }
    }
}
