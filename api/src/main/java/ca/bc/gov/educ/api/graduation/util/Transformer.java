package ca.bc.gov.educ.api.graduation.util;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface Transformer {

    public Object unmarshall(byte[] input, Class<?> clazz);

    public Object unmarshall(String input, Class<?> clazz);
    public Object unmarshall(String input, Class<?> clazz, Map<Class, List<String>> additionalIgnoreFields);

    public Object unmarshall(InputStream input, Class<?> clazz);

    public Object unmarshall(String input, TypeReference<?> valueTypeRef);

    public String marshall(Object input);

    public String getAccept();

    public String getContentType();

}
