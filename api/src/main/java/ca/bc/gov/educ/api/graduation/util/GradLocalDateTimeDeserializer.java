package ca.bc.gov.educ.api.graduation.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants.*;

public class GradLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    public GradLocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
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
            return LocalDateTime.parse(dateAsString, formatter);
        } else if(jsonParser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            long timestamp = jsonParser.getValueAsLong();
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        } else if(StringUtils.isNotBlank(dateAsString) && dateAsString.length() == 10 && dateAsString.contains("-")) {
            formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
            LocalDate localDate = LocalDate.parse(dateAsString, formatter);
            return localDate.atStartOfDay();
        } else if(StringUtils.isNotBlank(dateAsString) && dateAsString.length() == 10 && dateAsString.contains("/")) {
            formatter = DateTimeFormatter.ofPattern(SECOND_DEFAULT_DATE_FORMAT);
            LocalDate localDate = LocalDate.parse(dateAsString, formatter);
            return localDate.atStartOfDay();
        } else if(StringUtils.isNotBlank(dateAsString) && dateAsString.length() > 10 && dateAsString.length() <= 19 && dateAsString.contains("/") && dateAsString.contains(" ")) {
            formatter = DateTimeFormatter.ofPattern(SECOND_DEFAULT_DATE_TIME_FORMAT);
            return LocalDateTime.parse(dateAsString, formatter);
        } else if(StringUtils.isNotBlank(dateAsString) && dateAsString.length() > 10 && dateAsString.length() <= 19 && dateAsString.contains("-") && dateAsString.contains(" ")) {
            formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
            return LocalDateTime.parse(dateAsString, formatter);
        } else if(StringUtils.isNotBlank(dateAsString) && dateAsString.length() > 19 && dateAsString.contains("/") && dateAsString.contains("T")) {
            formatter = DateTimeFormatter.ofPattern(SECOND_DEFAULT_DATE_TIME_FORMAT);
            return LocalDateTime.parse(StringUtils.replace(StringUtils.substringBefore(dateAsString, "."), "T", " "), formatter);
        } else if(StringUtils.isNotBlank(dateAsString) && dateAsString.length() > 19 && dateAsString.contains("-") && dateAsString.contains("T")) {
            formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
            return LocalDateTime.parse(StringUtils.replace(StringUtils.substringBefore(dateAsString, "."), "T", " "), formatter);
        } else if(StringUtils.isNotBlank(dateAsString)) {
            return LocalDateTime.parse(dateAsString, formatter);
        }
        return null;
    }
}
