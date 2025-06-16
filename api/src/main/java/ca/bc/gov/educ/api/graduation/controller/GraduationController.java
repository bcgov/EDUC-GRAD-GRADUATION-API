package ca.bc.gov.educ.api.graduation.controller;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.graduation.model.dto.institute.District;
import ca.bc.gov.educ.api.graduation.model.dto.institute.YearEndReportRequest;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.model.report.School;
import ca.bc.gov.educ.api.graduation.service.DistrictReportService;
import ca.bc.gov.educ.api.graduation.service.GraduationService;
import ca.bc.gov.educ.api.graduation.service.ReportService;
import ca.bc.gov.educ.api.graduation.service.SchoolReportsService;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.PermissionsContants;
import ca.bc.gov.educ.api.graduation.util.ResponseHelper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.graduation.service.SchoolReportsService.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping(EducGraduationApiConstants.GRADUATION_API_ROOT_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for Graduating Student.", description = "This API is for Graduating Student.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"GRAD_GRADUATE_STUDENT"})})
public class GraduationController {

    GraduationService gradService;
    SchoolReportsService schoolReportsService;
    DistrictReportService districtReportService;
    ReportService reportService;

    ResponseHelper response;

    public GraduationController(GraduationService gradService, SchoolReportsService schoolReportsService,
                                DistrictReportService districtReportService, ReportService reportService,
                                ResponseHelper response) {
        this.gradService = gradService;
        this.schoolReportsService = schoolReportsService;
        this.districtReportService = districtReportService;
        this.reportService = reportService;
        this.response = response;
    }

    @GetMapping(EducGraduationApiConstants.GRADUATE_STUDENT_BY_STUDENT_ID_AND_PROJECTED_TYPE)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "Run different Grad Runs and Graduate Student by Student ID and projected type", description = "Run different Grad Runs and Graduate Student by Student ID and projected type", tags = { "Graduation" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<AlgorithmResponse> graduateStudentNew(@PathVariable String studentID, @PathVariable String projectedType,
                                                                @RequestParam(required = false) Long batchId) {
        log.debug("Graduate Student for Student ID: {}", studentID);
        return response.GET(gradService.graduateStudent(studentID,batchId,projectedType));
    }

    @GetMapping(EducGraduationApiConstants.GRADUATE_REPORT_DATA_BY_PEN)
    @PreAuthorize(PermissionsContants.GRADUATE_DATA)
    @Operation(summary = "Get Report data from graduation by student pen", description = "Get Report data from graduation by student pen", tags = { "Graduation Data" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<ReportData> reportDataByPen(@PathVariable @NotNull String pen, @RequestParam(required = false) String type) {
        log.debug("Report Data By Student Pen: {}", pen);
        ReportData resultReportData = gradService.prepareReportData(pen, type);
        if (resultReportData == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return response.GET(resultReportData);
    }

    @GetMapping(EducGraduationApiConstants.GRADUATE_TRANSCRIPT_REPORT)
    @PreAuthorize(PermissionsContants.GRADUATE_TRANSCRIPT)
    @Operation(summary = "Get Transcript binary from graduation by student pen", description = "Get Transcript binary from graduation by student pen", tags = { "Graduation Data" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> reportTranscriptByPen(@PathVariable @NotNull String pen,
                                                        @RequestParam(required = false) String interim,
                                                        @RequestParam(required = false) String preview) {
        log.debug("Report Data By Student Pen: {}", pen);
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
        log.debug("Report Data from graduation for student: {}", graduationData.getGradStudent().getStudentID());
        ReportData resultReportData = gradService.prepareReportData(graduationData, type);
        if (resultReportData == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return response.GET(resultReportData);
    }

    @PostMapping(EducGraduationApiConstants.SCHOOL_REPORTS)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Report Creation", description = "When triggered, School Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolReports(@RequestBody List<UUID> uniqueSchools, @RequestParam(required = false) String type ) {
        return response.GET(gradService.createAndStoreSchoolReports(uniqueSchools,type));
    }

    @PostMapping(EducGraduationApiConstants.SCHOOL_REPORTS_LABELS)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Labels Report Creation", description = "When triggered, School Labels Reports are created from list of Schools", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolLabelsReports(@RequestBody List<School> schools, @RequestParam String reportType ) {
        return response.GET(schoolReportsService.createAndStoreSchoolLabelsReportsFromSchools(reportType, schools, null));
    }

    @PostMapping(EducGraduationApiConstants.DISTRICT_REPORTS_LABELS)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "District Labels Report Creation", description = "When triggered, District Labels Reports are created from list of districts", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreDistrictLabelsReports(@RequestBody List<District> districts, @RequestParam String reportType ) {
        return response.GET(districtReportService.createAndStoreDistrictLabelsReportsFromDistricts(reportType, districts, null));
    }

    @PostMapping(EducGraduationApiConstants.DISTRICT_SCHOOL_REPORTS_LABELS)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "District Label Report Creation for Schools", description = "When triggered, District Labels Reports are created from list of schools", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreDistrictLabelsReportsBySchools(@RequestBody List<School> schools, @RequestParam UUID districtId, @RequestParam String reportType) {
        return response.GET(districtReportService.createAndStoreDistrictLabelsReportsFromSchools(reportType, districtId, schools));
    }

    @PostMapping(EducGraduationApiConstants.SCHOOL_REPORTS_LABELS_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Labels Report PDF", description = "When triggered, School Labels Reports PDF are created from list of Schools", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getSchoolLabelsReports(@RequestBody List<School> schools, @RequestParam String reportType ) throws IOException {
        byte[] resultBinary = schoolReportsService.getSchoolLabelsReportsFromSchools(reportType, schools);
        return handleBinaryResponse(resultBinary, "SchoolLabelsReports.pdf", MediaType.APPLICATION_PDF);
    }

    @PostMapping(EducGraduationApiConstants.DISTRICT_REPORTS_LABELS_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "District Labels Report PDF", description = "When triggered, District Labels Reports PDF are created from list of districts", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getDistrictLabelsReports(@RequestBody List<District> districts, @RequestParam String reportType ) throws IOException {
        byte[] resultBinary = districtReportService.getDistrictLabelsReportsFromDistricts(reportType, districts);
        return handleBinaryResponse(resultBinary, "SchoolLabelsReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_REPORTS_YEAR_END)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Year End Report Creation", description = "When triggered, School Year End Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolYearEndReports() {
        return response.GET(schoolReportsService.createAndStoreSchoolReports(DISTREP_YE_SC));
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_REPORTS_MONTH)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Monthly Report Creation", description = "When triggered, School Monthly Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolReports() {
        return response.GET(schoolReportsService.createAndStoreSchoolReports(DISTREP_SC));
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_REPORTS_YEAR_END_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Year End Report Generation (PDF)", description = "When triggered, School Year End Reports returns in PDF", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getSchoolYearEndReports() throws IOException {
        byte[] resultBinary = schoolReportsService.getSchoolYearEndReports();
        return handleBinaryResponse(resultBinary, "SchoolYearEndReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_REPORTS_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Report Generation (PDF)", description = "When triggered, School Reports returns in PDF", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getSchoolReports() throws IOException {
        byte[] resultBinary = schoolReportsService.getSchoolReports();
        return handleBinaryResponse(resultBinary, "SchoolYearEndReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.DISTRICT_REPORTS_YEAR_END)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "District Year End Report Creation", description = "When triggered, District Year End Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreDistrictYearEndReports() {
        return response.GET(districtReportService.createAndStoreDistrictYearEndReports());
    }

    @GetMapping(EducGraduationApiConstants.DISTRICT_REPORTS_YEAR_END_NONGRAD)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "District Year End NonGrad Report Creation", description = "When triggered, District Year End NonGrad Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreDistrictYearEndNonGradReports() {
        return response.GET(districtReportService.createAndStoreDistrictNonGradYearEndReport());
    }

    @GetMapping(EducGraduationApiConstants.DISTRICT_REPORTS_MONTH)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "District Monthly Report Creation", description = "When triggered, District Monthly Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreDistrictReports() {
        return response.GET(districtReportService.createAndStoreDistrictReportMonth());
    }

    @GetMapping(EducGraduationApiConstants.DISTRICT_REPORTS_YEAR_END_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "District Year End Report Generation (PDF)", description = "When triggered, District Year End Reports returns in PDF", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getDistrictYearEndReports() throws IOException {
        byte[] resultBinary = districtReportService.getDistrictYearEndReports();
        return handleBinaryResponse(resultBinary, "DistrictYearEndReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.DISTRICT_REPORTS_YEAR_END_NONGRAD_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "District Year End NonGrad Report Generation (PDF)", description = "When triggered, District Year End NonGrad Reports returns in PDF", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getDistrictYearEndNonGradReports() throws IOException {
        byte[] resultBinary = districtReportService.getDistrictYearEndNonGradReports();
        return handleBinaryResponse(resultBinary, "DistrictYearEndNonGradReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.DISTRICT_REPORTS_MONTH_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "District Monthly Report Generation (PDF)", description = "When triggered, District Monthly Reports returns in PDF", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getDistrictReports() throws IOException {
        byte[] resultBinary = districtReportService.getDistrictReports();
        return handleBinaryResponse(resultBinary, "DistrictReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_YEAR_END)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Year End Report Creation", description = "When triggered, School & District Year End Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolDistrictYearEndReports(
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt) {
        return response.GET(schoolReportsService.createAndStoreSchoolDistrictYearEndReports(slrt, drt, srt));
    }

    @PostMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_YEAR_END)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Year End Report Creation by Request", description = "When triggered, School & District Year End Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolDistrictYearEndReports(
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt,
            @RequestBody YearEndReportRequest yearEndReportRequest) {
        return response.GET(schoolReportsService.createAndStoreSchoolDistrictYearEndReports(slrt, drt, srt, yearEndReportRequest));
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_MONTH)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Monthly Report Creation", description = "When triggered, School & District Monthly Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolDistrictReports(
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt
    ) {
        return response.GET(schoolReportsService.createAndStoreSchoolDistrictReports(slrt, drt, srt));
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_SUPP)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Supplemental Report Creation", description = "When triggered, School & District Supplemental Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolDistrictSuppReports(
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt
    ) {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport();
        return response.GET(schoolReportsService.createAndStoreSchoolDistrictReports(reportGradStudentDataList, slrt, drt, srt));
    }

    @GetMapping(EducGraduationApiConstants.STUDENT_FOR_YEAR_END_REPORT)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "Students for year end reports", description = "When triggered, list of students, eligible for the year end reports returns", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<ReportGradStudentData>> getStudentsForYearEndReports() {
        return response.GET(reportService.getStudentsForSchoolYearEndReport());
    }

    @PostMapping(EducGraduationApiConstants.STUDENT_FOR_YEAR_END_REPORT)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "Students for year end reports by search criteria", description = "When triggered, list of students, eligible for the year end reports returns", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<ReportGradStudentData>> getStudentsForYearEndReports(@RequestBody YearEndReportRequest yearEndReportRequest) {
        return response.GET(reportService.getStudentsForSchoolYearEndReport(yearEndReportRequest));
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_NONGRAD_YEAR_END)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Year End NonGrad Report Creation", description = "When triggered, School & District Year End NonGrad Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolDistrictYearEndNonGradReports(
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt
    ) {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolNonGradYearEndReport();
        return response.GET(schoolReportsService.createAndStoreSchoolDistrictReports(reportGradStudentDataList, slrt, drt, srt));
    }

    @PostMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_NONGRAD_YEAR_END)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Year End NonGrad Report Creation by Request", description = "When triggered, School & District Year End NonGrad Reports are created by Request", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolDistrictYearEndNonGradReports(
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt,
            @RequestBody List<UUID> schools) {
        List<ReportGradStudentData> reportGradStudentDataTotalList = new ArrayList<>();
        for(UUID schoolId: schools) {
            List<ReportGradStudentData> sd = reportService.getStudentsForSchoolNonGradYearEndReport(schoolId);
            reportGradStudentDataTotalList.addAll(sd);
        }
        return response.GET(schoolReportsService.createAndStoreSchoolDistrictReports(reportGradStudentDataTotalList, slrt, drt, srt));
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_NONGRAD_YEAR_END_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Year End NonGrad Report Retrieval (PDF)", description = "When triggered, School & District Year End NonGrad Reports generated on fly", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getSchoolDistrictYearEndNonGradReports(
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt) throws IOException {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolNonGradYearEndReport();
        byte[] resultBinary = schoolReportsService.getSchoolDistrictReports(reportGradStudentDataList, slrt, drt, srt);
        return handleBinaryResponse(resultBinary, "DistrictSchoolYearEndNonGradReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_YEAR_END_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Year End Report Retrieval (PDF)", description = "When triggered, School & District Year End Reports generated on fly", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getSchoolDistrictYearEndReports(
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt) throws IOException {
        byte[] resultBinary = schoolReportsService.getSchoolDistrictYearEndReports(slrt, drt, srt);
        return handleBinaryResponse(resultBinary, "DistrictSchoolYearEndReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_MONTH_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Monthly Report Retrieval (PDF)", description = "When triggered, School & District Monthly Reports generated on fly", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getSchoolDistrictReports(
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt) throws IOException {
        byte[] resultBinary = schoolReportsService.getSchoolDistrictReports(slrt, drt, srt);
        return handleBinaryResponse(resultBinary, "DistrictSchoolReports.pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping(EducGraduationApiConstants.SCHOOL_AND_DISTRICT_REPORTS_SUPP_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School & District Supplemental Report Retrieval (PDF)", description = "When triggered, School & District Monthly Reports generated on fly", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getSchoolDistrictSuppReports(
            @RequestParam(required = false) String slrt,
            @RequestParam(required = false) String drt,
            @RequestParam(required = false) String srt) throws IOException {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport();
        byte[] resultBinary = schoolReportsService.getSchoolDistrictReports(reportGradStudentDataList, slrt, drt, srt);
        return handleBinaryResponse(resultBinary, "DistrictSchoolReports.pdf", MediaType.APPLICATION_PDF);
    }

    @PostMapping(EducGraduationApiConstants.SCHOOL_REPORTS_PDF)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Report Generation (PDF) by SchoolIds", description = "When triggered, School Report is generated by SchoolIds", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> getSchoolReports(@RequestBody List<UUID> uniqueSchools, @RequestParam(required = true) String type ) {
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
