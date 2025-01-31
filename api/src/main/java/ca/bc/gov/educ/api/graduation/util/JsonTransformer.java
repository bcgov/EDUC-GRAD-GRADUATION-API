package ca.bc.gov.educ.api.graduation.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class JsonTransformer implements Transformer {

    private static final Logger log = LoggerFactory.getLogger(JsonTransformer.class);
    private static final String MARSHALLING_MSG = "Time taken for unmarshalling response from String to {} is {} ms";

    final ObjectMapper objectMapper;

    @Autowired
    public JsonTransformer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Object unmarshall(byte[] input, Class<?> clazz) {
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = objectMapper.readValue(input, clazz);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        log.debug("Time taken for unmarshalling response from bytes to {} is {} ms", clazz.getName(), (System.currentTimeMillis() - start));
        return result;
    }


    @Override
    public Object unmarshall(String input, Class<?> clazz) {
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = objectMapper.readValue(input, clazz);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        log.debug(MARSHALLING_MSG, clazz.getName(), (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public Object unmarshall(String input, Class<?> clazz, Map<Class, List<String>> additionalIgnoreFields) {
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector(){
                @Override
                public boolean hasIgnoreMarker(final AnnotatedMember m) {
                    List<String> ignoreFields = additionalIgnoreFields.getOrDefault(m.getDeclaringClass(), List.of());
                    return ignoreFields.contains(m.getName()) || super.hasIgnoreMarker(m);
                }
            });
            result = objectMapper.readValue(input, clazz);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        log.debug(MARSHALLING_MSG, clazz.getName(), (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public Object unmarshall(String input, TypeReference<?> valueTypeRef) {
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = objectMapper.readValue(input, valueTypeRef);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        log.debug(MARSHALLING_MSG, valueTypeRef.getType().getTypeName(), (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public Object unmarshall(InputStream input, Class<?> clazz) {
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = objectMapper.readValue(input, clazz);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        log.debug("Time taken for unmarshalling response from stream to {} is {} ms", clazz.getName(), (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public String marshall(Object input) {
        String result = null;
        try {
            result = objectMapper.writeValueAsString(input);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
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

    public <T> T convertValue(Object fromValue, TypeReference<T> toValueTypeRef) throws IllegalArgumentException {
        return objectMapper.convertValue(fromValue, toValueTypeRef);
    }
}
