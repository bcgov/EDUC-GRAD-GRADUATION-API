package ca.bc.gov.educ.api.graduation.util;

import java.util.Date;

public class EducGraduationApiConstants {

    //API end-point Mapping constants
	public static final String API_ROOT_MAPPING = "";
    public static final String API_VERSION = "v1";
    public static final String GRADUATION_API_ROOT_MAPPING = "/api/" + API_VERSION + "/graduate";
    public static final String GRADUATE_STUDENT_BY_PEN = "/pen/{pen}";
    public static final String STUDENT_ACHIEVEMENT_REPORT_BY_PEN = "pen/{pen}/achievementreport";
    public static final String STUDENT_TRANSCRIPT_BY_PEN = "pen/{pen}/transcript";

    public static final String ENDPOINT_GRADUATION_ALGORITHM_URL = "${endpoint.gradalgorithm-api.gradalgorithm}";
    public static final String ENDPOINT_GRAD_STATUS_UPDATE_URL = "${endpoint.graduation-status-api.update-grad-status}";
    public static final String ENDPOINT_GRAD_STUDENT_REPORT_UPDATE_URL = "${endpoint.grad-common-api.update-grad-student-report.url}";
    public static final String ENDPOINT_REPORT_API_URL = "${endpoint.report-api.report}";
    public static final String ENDPOINT_GRAD_CERTIFICATE_TYPE_URL = "${endpoint.grad-code-api.get-certificate-name}";
    public static final String ENDPOINT_GRAD_PROGRAM_NAME_URL = "${endpoint.grad-program-management-api.program_name_by_program_code.url}";
    
    //Default Attribute value constants
    public static final String DEFAULT_CREATED_BY = "GraduationAPI";
    public static final Date DEFAULT_CREATED_TIMESTAMP = new Date();
    public static final String DEFAULT_UPDATED_BY = "GraduationAPI";
    public static final Date DEFAULT_UPDATED_TIMESTAMP = new Date();

    //Default Date format constants
    public static final String DEFAULT_DATE_FORMAT = "dd-MMM-yyyy";
    
    public static final String TRAX_DATE_FORMAT = "yyyyMM";
	
}
