package ca.bc.gov.educ.api.graduation.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class GradLocalDateSerializer extends JsonSerializer<LocalDate> {

    @Override
    public void serialize(LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        String fieldName = jsonGenerator.getOutputContext().getCurrentName();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        if(localDate != null) {
            String value = localDate.format(formatter);
            log.debug("Serialize LocalDate of {} to value {}", fieldName, value);
            jsonGenerator.writeString(value);
        }
    }
}
