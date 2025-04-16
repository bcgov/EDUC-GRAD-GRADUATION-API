package ca.bc.gov.educ.api.graduation.model.dto.institute;

import ca.bc.gov.educ.api.graduation.model.dto.GradRequirement;
import ca.bc.gov.educ.api.graduation.util.GradLocalDateDeserializer;
import ca.bc.gov.educ.api.graduation.util.GradLocalDateSerializer;
import ca.bc.gov.educ.api.graduation.util.GradLocalDateTimeDeserializer;
import ca.bc.gov.educ.api.graduation.util.GradLocalDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class YearEndStudentCredentialDistribution {
    private UUID id;
    private String credentialTypeCode;
    private UUID studentID;
    private String paperType;
    private UUID schoolId;
    private String documentStatusCode;

    private String pen;
    private String legalFirstName;
    private String legalMiddleNames;
    private String legalLastName;
    private String studentCitizenship;
    @JsonSerialize(using = GradLocalDateSerializer.class)
    @JsonDeserialize(using = GradLocalDateDeserializer.class)
    private LocalDate programCompletionDate;
    @JsonSerialize(using = GradLocalDateTimeSerializer.class)
    @JsonDeserialize(using = GradLocalDateTimeDeserializer.class)
    private LocalDateTime lastUpdateDate;
    private String honoursStanding;
    private String program;
    private String studentGrade;
    private List<GradRequirement> nonGradReasons;

    private UUID schoolOfRecordId;
    private UUID schoolAtGradId;
    private UUID districtId;
    private UUID districtAtGradId;

    private String reportingSchoolTypeCode;

    private String transcriptTypeCode;
    private String certificateTypeCode;

}
