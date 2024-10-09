package ca.bc.gov.educ.api.graduation.controller;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.model.report.School;
import ca.bc.gov.educ.api.graduation.service.GraduationService;
import ca.bc.gov.educ.api.graduation.service.ReportService;
import ca.bc.gov.educ.api.graduation.service.SchoolReportsService;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import ca.bc.gov.educ.api.graduation.util.PermissionsContants;
import ca.bc.gov.educ.api.graduation.util.ResponseHelper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static ca.bc.gov.educ.api.graduation.service.SchoolReportsService.*;

@CrossOrigin
@RestController
@RequestMapping(EducGraduationApiConstants.GRADUATION_API_ROOT_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for Graduating Student.", description = "This API is for Graduating Student.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"GRAD_GRADUATE_STUDENT"})})
public class GraduationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraduationController.class);
    private static final String BEARER = "Bearer ";

    @Autowired
    GraduationService gradService;

    @Autowired
    SchoolReportsService schoolReportsService;

    @Autowired
    ReportService reportService;

    @Autowired
    GradValidation validation;

    @Autowired
    ResponseHelper response;

    @GetMapping(EducGraduationApiConstants.GRADUATE_STUDENT_BY_STUDENT_ID_AND_PROJECTED_TYPE)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "Run different Grad Runs and Graduate Student by Student ID and projected type", description = "Run different Grad Runs and Graduate Student by Student ID and projected type", tags = { "Graduation" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<AlgorithmResponse> graduateStudentNew(@PathVariable String studentID, @PathVariable String projectedType,
                                                                @RequestParam(required = false) Long batchId) {
        LOGGER.debug("Graduate Student for Student ID: {}", studentID);
        return response.GET(gradService.graduateStudent(studentID,batchId,projectedType));
    }

    @GetMapping(EducGraduationApiConstants.GRADUATE_REPORT_DATA_BY_PEN)
    @PreAuthorize(PermissionsContants.GRADUATE_DATA)
    @Operation(summary = "Get Report data from graduation by student pen", description = "Get Report data from graduation by student pen", tags = { "Graduation Data" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<ReportData> reportDataByPen(@PathVariable @NotNull String pen, @RequestParam(required = false) String type) {
        LOGGER.debug("Report Data By Student Pen: {}", pen);
        return response.GET(gradService.prepareReportData(pen, type));
    }

    @GetMapping(EducGraduationApiConstants.GRADUATE_TRANSCRIPT_REPORT)
    @PreAuthorize(PermissionsContants.GRADUATE_TRANSCRIPT)
    @Operation(summary = "Get Transcript binary from graduation by student pen", description = "Get Transcript binary from graduation by student pen", tags = { "Graduation Data" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> reportTranscriptByPen(@PathVariable @NotNull String pen,
                                                        @RequestParam(required = false) String interim,
                                                        @RequestParam(required = false) String preview) {
        LOGGER.debug("Report Data By Student Pen: {}", pen);
        byte[] resultBinary = gradService.prepareTranscriptReport(pen, interim, preview);
        if(resultBinary == null || resultBinary.length == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        byte[] encoded = Base64.encodeBase64(resultBinary);
        return handleBinaryResponse(encoded, String.format("%sTranscript%sReport.pdfencoded", pen, interim), MediaType.TEXT_PLAIN);
    }

    @PostMapping(EducGraduationApiConstants.GRADUATE_REPORT_DATA)
    @PreAuthorize(PermissionsContants.GRADUATE_DATA)
    @Operation(summary = "Adapt graduation data for reporting", description = "Adapt graduation data for reporting", tags = { "Graduation Data" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<ReportData> reportDataFromGraduation(@RequestBody @NotNull GraduationData graduationData,
                                                               @RequestParam(required = false) String type) {
        LOGGER.debug("Report Data from graduation for student: {}", graduationData.getGradStudent().getStudentID());
        return response.GET(gradService.prepareReportData(graduationData, type));
    }

    @PostMapping(EducGraduationApiConstants.SCHOOL_REPORTS)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Report Creation", description = "When triggered, School Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolReports(@RequestBody List<String> uniqueSchools, @RequestHeader(name="Authorization") String accessToken,@RequestParam(required = false) String type ) {
        return response.GET(gradService.createAndStoreSchoolReports(uniqueSchools,type));
    }

    @PostMapping(EducGraduationApiConstants.SCHOOL_REPORTS_LABELS)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Labels Report Creation", description = "When triggered, School Labels Reports are created from list of Schools", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolLabelsReports(@RequestBody List<School> schools, @RequestHeader(name="Authorization") String accessToken, @RequestParam String reportType ) {
        return response.GET(schoolReportsService.createAndStoreSchoolLabelsReportsFromSchools(reportType, schools, accessToken.replace(BEARER, ""), null));
    }

    @PostMapping(EducGraduationApiConstants.SCHOOL_REPORTS_LABELS_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Labels Report Creation", description = "When triggered, School Labels Reports are created from list of Schools", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getSchoolLabelsReports(@RequestBody List<School> schools, @RequestHeader(name="Authorization") String accessToken, @RequestParam String reportType ) {
        byte[] resultBinary = schoolReportsService.getSchoolLabelsReportsFromSchools(reportType, schools, accessToken.replace(BEARER, ""));
        return handleBinaryResponse(resultBinary, "SchoolLabelsReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_REPORTS_YEAR_END)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Year End Report Creation", description = "When triggered, School Year End Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolYearEndReports(@RequestHeader(name="Authorization") String accessToken) {
        return response.GET(schoolReportsService.createAndStoreSchoolReports(DISTREP_YE_SC, accessToken.replace(BEARER, "")));
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_REPORTS_MONTH)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Year End Report Creation", description = "When triggered, School Year End Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolReports(@RequestHeader(name="Authorization") String accessToken) {
        return response.GET(schoolReportsService.createAndStoreSchoolReports(DISTREP_SC, accessToken.replace(BEARER, "")));
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_REPORTS_YEAR_END_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Year End Report Generation (PDF)", description = "When triggered, School Year End Reports returns in PDF", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getSchoolYearEndReports(@RequestHeader(name="Authorization") String accessToken) {
        byte[] resultBinary = schoolReportsService.getSchoolYearEndReports(accessToken.replace(BEARER, ""));
        return handleBinaryResponse(resultBinary, "SchoolYearEndReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_REPORTS_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Year End Report Generation (PDF)", description = "When triggered, School Year End Reports returns in PDF", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getSchoolReports(@RequestHeader(name="Authorization") String accessToken) {
        byte[] resultBinary = schoolReportsService.getSchoolReports(accessToken.replace(BEARER, ""));
        return handleBinaryResponse(resultBinary, "SchoolYearEndReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.DISTRICT_REPORTS_YEAR_END)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "District Report Creation", description = "When triggered, District Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreDistrictYearEndReports(@RequestHeader(name="Authorization") String accessToken) {
        return response.GET(schoolReportsService.createAndStoreDistrictReports(DISTREP_YE_SD, accessToken.replace(BEARER, "")));
    }

    @GetMapping(EducGraduationApiConstants.DISTRICT_REPORTS_YEAR_END_NONGRAD)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "District Report Creation", description = "When triggered, District Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreDistrictYearEndNonGradReports(@RequestHeader(name="Authorization") String accessToken) {
        return response.GET(schoolReportsService.createAndStoreDistrictReports(NONGRADDISTREP_SD, accessToken.replace(BEARER, "")));
    }

    @GetMapping(EducGraduationApiConstants.DISTRICT_REPORTS_MONTH)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "District Report Creation", description = "When triggered, District Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreDistrictReports(@RequestHeader(name="Authorization") String accessToken) {
        return response.GET(schoolReportsService.createAndStoreDistrictReports(DISTREP_SD, accessToken.replace(BEARER, "")));
    }

    @GetMapping(EducGraduationApiConstants.DISTRICT_REPORTS_YEAR_END_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "District Report Generation (PDF)", description = "When triggered, District Reports returns in PDF", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getDistrictYearEndReports(@RequestHeader(name="Authorization") String accessToken) {
        byte[] resultBinary = schoolReportsService.getDistrictYearEndReports(accessToken.replace(BEARER, ""));
        return handleBinaryResponse(resultBinary, "DistrictYearEndReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.DISTRICT_REPORTS_YEAR_END_NONGRAD_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "District Report Generation (PDF)", description = "When triggered, District Reports returns in PDF", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getDistrictYearEndNonGradReports(@RequestHeader(name="Authorization") String accessToken) {
        byte[] resultBinary = schoolReportsService.getDistrictYearEndNonGradReports(accessToken.replace(BEARER, ""));
        return handleBinaryResponse(resultBinary, "DistrictYearEndNonGradReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.DISTRICT_REPORTS_MONTH_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "District Report Generation (PDF)", description = "When triggered, District Reports returns in PDF", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getDistrictReports(@RequestHeader(name="Authorization") String accessToken) {
        byte[] resultBinary = schoolReportsService.getDistrictReports(accessToken.replace(BEARER, ""));
        return handleBinaryResponse(resultBinary, "DistrictReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_YEAR_END)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Report Creation", description = "When triggered, School & District Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolDistrictYearEndReports(
            @RequestHeader(name="Authorization") String accessToken,
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt) {
        return response.GET(schoolReportsService.createAndStoreSchoolDistrictYearEndReports(accessToken.replace(BEARER, ""), slrt, drt, srt));
    }

    @PostMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_YEAR_END)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Report Creation", description = "When triggered, School & District Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolDistrictYearEndReports(
            @RequestHeader(name="Authorization") String accessToken,
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt,
            @RequestBody List<String> schools) {
        return response.GET(schoolReportsService.createAndStoreSchoolDistrictYearEndReports(accessToken.replace(BEARER, ""), slrt, drt, srt, schools));
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_MONTH)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Report Creation", description = "When triggered, School & District Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolDistrictReports(
            @RequestHeader(name="Authorization") String accessToken,
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt
    ) {
        return response.GET(schoolReportsService.createAndStoreSchoolDistrictReports(accessToken.replace(BEARER, ""), slrt, drt, srt));
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_SUPP)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Report Creation", description = "When triggered, School & District Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolDistrictSuppReports(
            @RequestHeader(name="Authorization") String accessToken,
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt
    ) {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport(accessToken.replace(BEARER, ""));
        return response.GET(schoolReportsService.createAndStoreSchoolDistrictReports(accessToken.replace(BEARER, ""), reportGradStudentDataList, slrt, drt, srt));
    }

    @GetMapping(EducGraduationApiConstants.STUDENT_FOR_YEAR_END_REPORT)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "Students for year end reports", description = "When triggered, list of students, eligible for the year end reports returns", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<ReportGradStudentData>> getStudentsForYearEndReports(@RequestHeader(name="Authorization") String accessToken) {
        return response.GET(reportService.getStudentsForSchoolYearEndReport(accessToken.replace(BEARER, "")));
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_NONGRAD_YEAR_END)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Report Creation", description = "When triggered, School & District Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolDistrictYearEndNonGradReports(
            @RequestHeader(name="Authorization") String accessToken,
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt
    ) {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolNonGradYearEndReport();
        return response.GET(schoolReportsService.createAndStoreSchoolDistrictReports(accessToken.replace(BEARER, ""), reportGradStudentDataList, slrt, drt, srt));
    }

    @PostMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_NONGRAD_YEAR_END)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Report Creation", description = "When triggered, School & District Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolDistrictYearEndNonGradReports(
            @RequestHeader(name="Authorization") String accessToken,
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt,
            @RequestBody List<String> schools) {
        List<ReportGradStudentData> reportGradStudentDataTotalList = new ArrayList<>();
        for(String mincode: schools) {
            List<ReportGradStudentData> sd = reportService.getStudentsForSchoolNonGradYearEndReport(mincode);
            reportGradStudentDataTotalList.addAll(sd);
        }
        return response.GET(schoolReportsService.createAndStoreSchoolDistrictReports(accessToken.replace(BEARER, ""), reportGradStudentDataTotalList, slrt, drt, srt));
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_NONGRAD_YEAR_END_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Report Retrieval", description = "When triggered, School & District Reports generated on fly", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getSchoolDistrictYearEndNonGradReports(
            @RequestHeader(name="Authorization") String accessToken,
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt) {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolNonGradYearEndReport();
        byte[] resultBinary = schoolReportsService.getSchoolDistrictReports(accessToken.replace(BEARER, ""), reportGradStudentDataList, slrt, drt, srt);
        return handleBinaryResponse(resultBinary, "DistrictSchoolYearEndNonGradReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_YEAR_END_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Report Retrieval", description = "When triggered, School & District Reports generated on fly", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getSchoolDistrictYearEndReports(
            @RequestHeader(name="Authorization") String accessToken,
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt) {
        byte[] resultBinary = schoolReportsService.getSchoolDistrictYearEndReports(accessToken.replace(BEARER, ""), slrt, drt, srt);
        return handleBinaryResponse(resultBinary, "DistrictSchoolYearEndReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_MONTH_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Report Retrieval", description = "When triggered, School & District Reports generated on fly", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getSchoolDistrictReports(
            @RequestHeader(name="Authorization") String accessToken,
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt) {
        byte[] resultBinary = schoolReportsService.getSchoolDistrictReports(accessToken.replace(BEARER, ""), slrt, drt, srt);
        return handleBinaryResponse(resultBinary, "DistrictSchoolReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_SUPP_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Report Retrieval", description = "When triggered, School & District Reports generated on fly", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getSchoolDistrictSuppReports(
            @RequestHeader(name="Authorization") String accessToken,
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt) {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport(accessToken.replace(BEARER, ""));
        byte[] resultBinary = schoolReportsService.getSchoolDistrictReports(accessToken.replace(BEARER, ""), reportGradStudentDataList, slrt, drt, srt);
        return handleBinaryResponse(resultBinary, "DistrictSchoolReports.pdf", MediaType.APPLICATION_PDF);
    }

    @PostMapping(EducGraduationApiConstants.SCHOOL_REPORTS_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Report Generation", description = "When triggered, School Report is generated", tags = { "Reports", "type=GRADREG", "type=NONGRADREG", "type=NONGRADPRJ" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getSchoolReports(@RequestBody List<String> uniqueSchools, @RequestParam(required = true) String type ) {
        byte[] resultBinary = gradService.getSchoolReports(uniqueSchools,type);
        if(resultBinary == null || resultBinary.length == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return handleBinaryResponse(resultBinary, String.format("%sSchoolReport.pdf", type), MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.GRADUATE_CERTIFICATE_REPORT)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "Student Certificate Creation", description = "When triggered, Student Certificates are created for a given student", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreStudentCertificate(@PathVariable @NotNull String pen,
                                                                    @RequestParam(name="isOverwrite", required=false, defaultValue="N") String isOverwrite) {
        return response.GET(gradService.createAndStoreStudentCertificates(pen, "Y".equalsIgnoreCase(isOverwrite)));
    }

    private ResponseEntity<byte[]> handleBinaryResponse(byte[] resultBinary, String reportFile, MediaType contentType) {
        ResponseEntity<byte[]> responseEntity = null;

        if(resultBinary.length > 0) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=" + reportFile);
            responseEntity = ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(contentType)
                    .body(resultBinary);
        } else {
            responseEntity = ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return responseEntity;
    }
}
