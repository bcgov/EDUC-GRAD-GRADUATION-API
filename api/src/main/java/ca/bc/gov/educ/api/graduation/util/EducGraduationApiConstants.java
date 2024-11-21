package ca.bc.gov.educ.api.graduation.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Getter
@Setter
public class EducGraduationApiConstants {

    public static final String CORRELATION_ID = "correlationID";

    //API end-point Mapping constants
	public static final String API_ROOT_MAPPING = "";
    public static final String API_VERSION = "v1";
    public static final String GRADUATION_API_ROOT_MAPPING = "/api/" + API_VERSION + "/graduate";
    public static final String GRADUATE_STUDENT_BY_STUDENT_ID_AND_PROJECTED_TYPE = "/studentid/{studentID}/run/{projectedType}";
    public static final String GRADUATE_REPORT_DATA_BY_PEN = "/report/data/{pen}";
    public static final String GRADUATE_REPORT_DATA = "/report/data";
    public static final String GRADUATE_TRANSCRIPT_REPORT = "/report/transcript/{pen}";
    public static final String GRADUATE_CERTIFICATE_REPORT = "/report/certificate/{pen}";
    public static final String GRADUATE_ACHIEVEMENT_REPORT = "/report/achievement/{pen}";
    public static final String SCHOOL_REPORTS = "/report/school";

    public static final String SCHOOL_REPORTS_YEAR_END = "/report/schoolyearend";
    public static final String SCHOOL_REPORTS_MONTH = "/report/schoolmonth";
    public static final String SCHOOL_REPORTS_YEAR_END_PDF = "/report/schoolyearendpdf";
    public static final String SCHOOL_REPORTS_MONTH_PDF = "/report/schoolmonthpdf";
    public static final String DISTRICT_REPORTS_YEAR_END = "/report/districtyearend";
    public static final String DISTRICT_REPORTS_YEAR_END_NONGRAD = "/report/districtyearendnongrad";
    public static final String DISTRICT_REPORTS_MONTH = "/report/districtmonth";
    public static final String DISTRICT_REPORTS_YEAR_END_PDF = "/report/districtyearendpdf";
    public static final String DISTRICT_REPORTS_YEAR_END_NONGRAD_PDF = "/report/districtyearendnongradpdf";
    public static final String DISTRICT_REPORTS_MONTH_PDF = "/report/districtmonthpdf";
    public static final String SCHOOL_AND_DISTRICT_REPORTS_YEAR_END = "/report/schooldistrictyearend";
    public static final String SCHOOL_AND_DISTRICT_REPORTS_MONTH = "/report/schooldistrictmonth";
    public static final String SCHOOL_AND_DISTRICT_REPORTS_SUPP = "/report/schooldistrictsupp";
    public static final String SCHOOL_AND_DISTRICT_REPORTS_NONGRAD_YEAR_END = "/report/schooldistrictnongradyearend";
    public static final String SCHOOL_AND_DISTRICT_REPORTS_NONGRAD_YEAR_END_PDF = "/report/schooldistrictnongradyearendpdf";
    public static final String SCHOOL_AND_DISTRICT_REPORTS_YEAR_END_PDF = "/report/schooldistrictyearendpdf";
    public static final String SCHOOL_AND_DISTRICT_REPORTS_MONTH_PDF = "/report/schooldistrictmonthpdf";
    public static final String SCHOOL_AND_DISTRICT_REPORTS_SUPP_PDF = "/report/schooldistrictsupppdf";
    public static final String SCHOOL_REPORTS_PDF = "/report/school/pdf";
    public static final String SCHOOL_REPORTS_LABELS = "/report/school/labels";
    public static final String SCHOOL_REPORTS_LABELS_PDF = "/report/school/labels/pdf";
    public static final String STUDENT_FOR_YEAR_END_REPORT = "/report/studentsforyearend";
    public static final String EDW_GRADUATION_SNAPSHOT = "/edw/snapshot";
    
    @Value("${endpoint.gradalgorithm-api.gradalgorithm}")
    private String gradAlgorithmEndpoint;
    
    @Value("${endpoint.gradalgorithm-api.projectedgradalgorithm}")
    private String gradProjectedAlgorithmEndpoint;

    @Value("${endpoint.gradalgorithm-api.hypotheticalgradalgorithm}")
    private String gradHypotheticalAlgorithmEndpoint;
    
    @Value("${endpoint.grad-student-api.update-grad-status}")
    private String updateGradStatus;
    
    @Value("${endpoint.grad-student-api.update-grad-status-algo-error}")
    private String updateGradStatusAlgoError;

    @Value("${endpoint.grad-student-api.student-for-school-report}")
    private String gradStudentListSchoolReport;

    @Value("${endpoint.grad-student-api.student-count-for-school-report}")
    private String gradStudentCountSchoolReport;
    
    @Value("${endpoint.grad-student-api.read-grad-student-record}")
    private String readGradStudentRecord;
    
    @Value("${endpoint.grad-graduation-report-api.update-grad-student-report.url}")
    private String updateGradStudentReport;

    @Value("${endpoint.grad-graduation-report-api.update-grad-student-transcript.url}")
    private String updateGradStudentTranscript;
    
    @Value("${endpoint.grad-graduation-report-api.update-grad-student-certificate.url}")
    private String updateGradStudentCertificate;
    
    @Value("${endpoint.report-api.achievement_report}")
    private String achievementReport;

    @Value("${endpoint.report-api.student_non_grad_projected}")
    private String studentNonGradProjected;

    @Value("${endpoint.report-api.student_non_grad}")
    private String studentNonGrad;

    @Value("${endpoint.report-api.school_graduation}")
    private String schoolGraduation;

    @Value("${endpoint.report-api.school_non_graduation}")
    private String schoolNonGraduation;

    @Value("${endpoint.report-api.school_distribution_year_end}")
    private String schoolDistributionYearEnd;

    @Value("${endpoint.report-api.district_distribution_year_end}")
    private String districtDistributionYearEnd;

    @Value("${endpoint.report-api.district_distribution_year_end_nongrad}")
    private String districtDistributionYearEndNonGrad;

    @Value("${endpoint.report-api.school_labels}")
    private String schoolLabels;
    
    @Value("${endpoint.report-api.certificate_report}")
    private String certificateReport;
    
    @Value("${endpoint.report-api.transcript_report}")
    private String transcriptReport;
    
    @Value("${endpoint.grad-graduation-report-api.get-certificate-name}")
    private String certificateTypeEndpoint;
    
    @Value("${endpoint.grad-program-api.program_name_by_program_code.url}")
    private String programNameEndpoint;

    @Value("${endpoint.grad-program-api.program_requirement_codes.url}")
    private String programRequirementsEndpoint;
    
    @Value("${endpoint.grad-student-api.save-optional-program-grad-status}")
    private String saveOptionalProgramGradStatus;
    
    @Value("${endpoint.grad-student-api.get-optional-program-details}")
    private String getOptionalProgramDetails;

    @Value("${endpoint.grad-student-api.get-student-optional-programs}")
    private String studentOptionalPrograms;
    
    @Value("${endpoint.grad-graduation-report-api.get-cert-list}")
    private String certList;

    @Value("${endpoint.grad-graduation-report-api.get-transcript}")
    private String transcript;
    
    @Value("${endpoint.grad-student-graduation-api.get-special-cases.url}")
    private String specialCase;

    @Value("${endpoint.grad-student-api.update-grad-status-projected}")
    private String saveStudentRecordProjectedRun;

    @Value("${endpoint.grad-student-api.school-nongrad-year-end-students}")
    private String schoolNonGradYearEndStudents;

    @Value("${endpoint.grad-student-api.student-nongrad-report-data}")
    private String studentNonGradReportData;

    @Value("${endpoint.grad-student-api.student-nongrad-report-data-mincode}")
    private String studentNonGradReportDataMincode;

    @Value("${endpoint.grad-student-api.edw-snapshot-of-grad-status}")
    private String edwSnapshotOfGraduationStatus;

    @Value("${endpoint.pen-student-api.by-pen.url}")
    private String penStudentApiByPenUrl;

    @Value("${endpoint.grad-trax-api.school-clob-by-school-id.url}")
    private String schoolClobBySchoolIdUrl;

    @Value("${endpoint.grad-trax-api.search-schools-by-min-code.url}")
    private String schoolDetails;

    @Value("${endpoint.grad-trax-api.district-by-min-code.url}")
    private String districtDetails;

    @Value("${endpoint.grad-graduation-report-api.update-grad-school-report.url}")
    private String updateSchoolReport;

    @Value("${endpoint.grad-graduation-report-api.school-year-end-students.url}")
    private String schoolYearEndStudents;

    @Value("${endpoint.grad-graduation-report-api.school-students.url}")
    private String schoolStudents;

    @Value("${authorization.user}")
    private String userName;

    @Value("${authorization.password}")
    private String password;

    @Value("${endpoint.keycloak.getToken}")
    private String tokenUrl;

    // Splunk LogHelper Enabled
    @Value("${splunk.log-helper.enabled}")
    private boolean splunkLogHelperEnabled;

    //Default Attribute value constants
    public static final String DEFAULT_CREATED_BY = "GraduationAPI";
    protected static final Date DEFAULT_CREATED_TIMESTAMP = new Date();
    public static final String DEFAULT_UPDATED_BY = "GraduationAPI";
    protected static final Date DEFAULT_UPDATED_TIMESTAMP = new Date();

    //Default Date format constants
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String SECOND_DEFAULT_DATE_FORMAT = "yyyy/MM/dd";
    public static final String SECOND_DEFAULT_DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";
    public static final String SECONDARY_DATE_FORMAT = SECOND_DEFAULT_DATE_FORMAT;
    public static final String TRAX_DATE_FORMAT = "yyyyMM";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String BIRTHDATE_FORMAT = DEFAULT_DATE_FORMAT;
	
}
