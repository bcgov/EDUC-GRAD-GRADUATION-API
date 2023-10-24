package ca.bc.gov.educ.api.graduation.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants.DEFAULT_DATE_FORMAT;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdwGraduationSnapshot {
    private UUID studentID;

    private Integer gradYear;
    private String pen;
    private String graduationFlag;
    private String honoursStanding;
    private BigDecimal gpa;
    private String graduatedDate;

    @JsonFormat(pattern=DEFAULT_DATE_FORMAT)
    private LocalDate runDate;
    @JsonFormat(pattern=DEFAULT_DATE_FORMAT)
    private LocalDate sessionDate;

    private String schoolOfRecord;
}
