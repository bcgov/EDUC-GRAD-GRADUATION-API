package ca.bc.gov.educ.api.graduation.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 *
 * @author alex.rybakov
 */
public class JSonNullStringSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (value == null || "null".equalsIgnoreCase(value)) {
            jsonGenerator.writeString("");
        } else {
            jsonGenerator.writeString(value);
        }
    }
}

