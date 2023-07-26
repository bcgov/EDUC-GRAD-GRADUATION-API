package ca.bc.gov.educ.api.graduation.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants.SECOND_DEFAULT_DATE_FORMAT;

public class GradLocalDateDeserializer extends JsonDeserializer<LocalDate> {

    private static final Logger logger = LoggerFactory.getLogger(GradLocalDateTimeDeserializer.class);

    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        String dateAsString = jsonParser.getValueAsString();
        //Fix date format as programCompletion date YYYY/MM
        if(StringUtils.isNotBlank(dateAsString) && dateAsString.length() < 10 && dateAsString.contains("/")) {
            int year = StringUtils.substringBefore(dateAsString, "/").length();
            int slashCount = StringUtils.countMatches(dateAsString, "/");
            if(year == 4 && slashCount == 1) {
                dateAsString = dateAsString + "/01";
            }
            if(slashCount > 0) {
                formatter = DateTimeFormatter.ofPattern(SECOND_DEFAULT_DATE_FORMAT);
            }
            return LocalDate.parse(dateAsString, formatter);
        } else if(jsonParser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            long timestamp = jsonParser.getValueAsLong();
            return LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        } else if(StringUtils.isNotBlank(dateAsString) && dateAsString.length() == 10 && dateAsString.contains("-")) {
            return LocalDate.parse(dateAsString, formatter);
        } else if(StringUtils.isNotBlank(dateAsString) && dateAsString.length() == 10 && dateAsString.contains("/")) {
            formatter = DateTimeFormatter.ofPattern(SECOND_DEFAULT_DATE_FORMAT);
            return LocalDate.parse(dateAsString, formatter);
        } else if(StringUtils.isNotBlank(dateAsString)) {
            return LocalDate.parse(dateAsString, formatter);
        }
        return null;
    }
}
