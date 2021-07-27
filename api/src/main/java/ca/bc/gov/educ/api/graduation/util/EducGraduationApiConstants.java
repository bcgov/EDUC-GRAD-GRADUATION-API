package ca.bc.gov.educ.api.graduation.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class EducGraduationApiConstants {

    //API end-point Mapping constants
	public static final String API_ROOT_MAPPING = "";
    public static final String API_VERSION = "v1";
    public static final String GRADUATION_API_ROOT_MAPPING = "/api/" + API_VERSION + "/graduate";
    public static final String GRADUATE_STUDENT_BY_STUDENT_ID_AND_PROJECTED_TYPE = "/studentid/{studentID}/run/{projectedType}";

    
    @Value("${endpoint.gradalgorithm-api.gradalgorithm}")
    private String gradAlgorithmEndpoint;
    
    @Value("${endpoint.gradalgorithm-api.projectedgradalgorithm}")
    private String gradProjectedAlgorithmEndpoint;
    
    @Value("${endpoint.grad-student-api.update-grad-status}")
    private String updateGradStatus;
    
    @Value("${endpoint.grad-student-api.read-grad-student-record}")
    private String readGradStudentRecord;
    
    @Value("${endpoint.grad-graduation-report-api.update-grad-student-report.url}")
    private String updateGradStudentReport;
    
    @Value("${endpoint.grad-graduation-report-api.update-grad-student-certificate.url}")
    private String updateGradStudentCertificate;
    
    @Value("${endpoint.report-api.achievement_report}")
    private String achievementReport;
    
    @Value("${endpoint.report-api.certificate_report}")
    private String certificateReport;
    
    @Value("${endpoint.report-api.transcript_report}")
    private String transcriptReport;
    
    @Value("${endpoint.grad-code-api.get-certificate-name}")
    private String certificateTypeEndpoint;
    
    @Value("${endpoint.grad-program-api.program_name_by_program_code.url}")
    private String programNameEndpoint;
    
    @Value("${endpoint.grad-student-api.save-special-program-grad-status}")
    private String saveSpecialProgramGradStatus;
    
    @Value("${endpoint.grad-student-api.get-special-program-details}")
    private String getSpecialProgramDetails;    
    
    
    //Default Attribute value constants
    public static final String DEFAULT_CREATED_BY = "GraduationAPI";
    public static final Date DEFAULT_CREATED_TIMESTAMP = new Date();
    public static final String DEFAULT_UPDATED_BY = "GraduationAPI";
    public static final Date DEFAULT_UPDATED_TIMESTAMP = new Date();

    //Default Date format constants
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    
    public static final String TRAX_DATE_FORMAT = "yyyyMM";
	
}
