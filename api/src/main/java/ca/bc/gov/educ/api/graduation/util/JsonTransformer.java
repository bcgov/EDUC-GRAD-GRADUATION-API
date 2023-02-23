package ca.bc.gov.educ.api.graduation.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Component
public class JsonTransformer implements Transformer {

    private static final Logger log = LoggerFactory.getLogger(JsonTransformer.class);

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.INDENT_OUTPUT)
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
                .enable(JsonGenerator.Feature.ESCAPE_NON_ASCII)
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd h:mm:ss"))
                .setTimeZone(TimeZone.getDefault())
        //        .enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS)
        ;
    }

    @Override
    public Object unmarshall(byte[] input, Class<?> clazz) throws TransformerException {
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = OBJECT_MAPPER.readValue(input, clazz);
        } catch (IOException e) {
            throw new TransformerException(e);
        }
        log.info("Time taken for unmarshalling response from bytes to {} is {} ms", clazz.getName(), (System.currentTimeMillis() - start));
        return result;
    }

    public Object unmarshallWithWrapper(String input, Class<?> clazz) throws TransformerException {
        final ObjectReader reader = OBJECT_MAPPER.readerFor(clazz);
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = reader
                    .with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                    .with(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)
                    .readValue(input);
        } catch (IOException e) {
            throw new TransformerException(e);
        }
        log.info("Time taken for unmarshalling response from String to {} is {} ms", clazz.getSimpleName(), (System.currentTimeMillis() - start));
        return result;
    }

    public String marshallWithWrapper(Object input) throws TransformerException {
        ObjectWriter prettyPrinter = OBJECT_MAPPER.writer();
        String result = null;
        try {
            result = prettyPrinter
                    .with(SerializationFeature.WRAP_ROOT_VALUE)
                    .writeValueAsString(input);
        } catch (IOException e) {
            throw new TransformerException(e);
        }

        return result;
    }

    @Override
    public Object unmarshall(String input, Class<?> clazz) throws TransformerException {
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = OBJECT_MAPPER.readValue(input, clazz);
        } catch (IOException e) {
            throw new TransformerException(e);
        }
        log.info("Time taken for unmarshalling response from String to {} is {} ms", clazz.getName(), (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public Object unmarshall(String input, TypeReference<?> valueTypeRef) throws TransformerException {
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = OBJECT_MAPPER.readValue(input, valueTypeRef);
        } catch (IOException e) {
            throw new TransformerException(e);
        }
        log.info("Time taken for unmarshalling response from String to {} is {} ms", valueTypeRef.getType().getTypeName(), (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public Object unmarshall(InputStream input, Class<?> clazz) throws TransformerException {
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = OBJECT_MAPPER.readValue(input, clazz);
        } catch (IOException e) {
            throw new TransformerException(e);
        }
        log.info("Time taken for unmarshalling response from stream to {} is {} ms", clazz.getName(), (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public String marshall(Object input) throws TransformerException {
        ObjectWriter prettyPrinter = OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
        String result = null;
        try {
            result = prettyPrinter.writeValueAsString(input);
        } catch (IOException e) {
            throw new TransformerException(e);
        }

        return result;
    }

    @Override
    public String getAccept() {
        return "application/json";
    }

    @Override
    public String getContentType() {
        return "application/json";
    }
}
