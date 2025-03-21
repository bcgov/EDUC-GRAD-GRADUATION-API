package ca.bc.gov.educ.api.graduation.model.dto;

import ca.bc.gov.educ.api.graduation.model.report.NonGradReason;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportGradStudentData implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID graduationStudentRecordId;
    private String mincode;
    private String mincodeAtGrad;
    private String schoolOfRecordId;
    private String schoolAtGradId;
    private String reportingSchoolTypeCode;
    private UUID districtId;
    private String pen;
    private String firstName;
    private String middleName;
    private String lastName;
    private String studentGrade;
    private String studentStatus;
    private String districtName;
    private String schoolName;
    private String schoolAddress1;
    private String schoolAddress2;
    private String schoolCity;
    private String schoolProvince;
    private String schoolCountry;
    private String schoolPostal;
    private String programCode;
    private String programName;
    private String programCompletionDate;
    private String graduated;
    private String transcriptTypeCode;
    private String certificateTypeCode;
    private String paperType;
    private LocalDateTime updateDate;
    private List<GradCertificateType> certificateTypes;
    private List<NonGradReason> nonGradReasons;

    public LocalDateTime getUpdateDate() {
        return updateDate == null ? LocalDateTime.now() : updateDate;
    }

    @Override
    public String toString() {
        return "ReportGradStudentData {" +
                "graduationStudentRecordId=" + graduationStudentRecordId +
                ", pen='" + pen + '\'' +
                ", mincode='" + mincode + '\'' +
                ", programCode='" + programCode + '\'' +
                ", certificateTypeCode='" + certificateTypeCode + '\'' +
                ", paperType='" + paperType + '\'' +
                '}';
    }
}
