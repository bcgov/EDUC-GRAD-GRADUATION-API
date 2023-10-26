package ca.bc.gov.educ.api.graduation.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentNonGradReason {

    private UUID graduationStudentRecordId;
    private String pen;

    private String transcriptRule1;
    private String description1;
    private String gradRule1;
    private String projected1;

    private String transcriptRule2;
    private String description2;
    private String gradRule2;
    private String projected2;

    private String transcriptRule3;
    private String description3;
    private String gradRule3;
    private String projected3;

    private String transcriptRule4;
    private String description4;
    private String gradRule4;
    private String projected4;

    private String transcriptRule5;
    private String description5;
    private String gradRule5;
    private String projected5;

    private String transcriptRule6;
    private String description6;
    private String gradRule6;
    private String projected6;

    private String transcriptRule7;
    private String description7;
    private String gradRule7;
    private String projected7;

    private String transcriptRule8;
    private String description8;
    private String gradRule8;
    private String projected8;

    private String transcriptRule9;
    private String description9;
    private String gradRule9;
    private String projected9;

    private String transcriptRule10;
    private String description10;
    private String gradRule10;
    private String projected10;

    private String transcriptRule11;
    private String description11;
    private String gradRule11;
    private String projected11;

    private String transcriptRule12;
    private String description12;
    private String gradRule12;
    private String projected12;

}
