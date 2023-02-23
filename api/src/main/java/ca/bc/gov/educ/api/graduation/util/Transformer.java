package ca.bc.gov.educ.api.graduation.util;

import com.fasterxml.jackson.core.type.TypeReference;

import javax.xml.transform.TransformerException;
import java.io.InputStream;

public interface Transformer {

    public Object unmarshall(byte[] input, Class<?> clazz) throws TransformerException;

    public Object unmarshall(String input, Class<?> clazz) throws TransformerException;

    public Object unmarshall(InputStream input, Class<?> clazz) throws TransformerException;

    public Object unmarshall(String input, TypeReference<?> valueTypeRef) throws TransformerException;

    public String marshall(Object input) throws TransformerException;

    public String getAccept();

    public String getContentType();
}
