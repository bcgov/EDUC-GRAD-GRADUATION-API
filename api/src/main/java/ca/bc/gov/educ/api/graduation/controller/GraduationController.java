package ca.bc.gov.educ.api.graduation.controller;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.service.GraduationService;
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

import javax.validation.constraints.NotNull;
import java.util.List;

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
    GradValidation validation;

    @Autowired
    ResponseHelper response;

    @GetMapping(EducGraduationApiConstants.GRADUATE_STUDENT_BY_STUDENT_ID_AND_PROJECTED_TYPE)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "Run different Grad Runs and Graduate Student by Student ID and projected type", description = "Run different Grad Runs and Graduate Student by Student ID and projected type", tags = { "Graduation" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<AlgorithmResponse> graduateStudentNew(@PathVariable String studentID, @PathVariable String projectedType,
                                                                @RequestParam(required = false) Long batchId,
                                                                @RequestHeader(name="Authorization") String accessToken) {
        LOGGER.debug("Graduate Student for Student ID: {}", studentID);
        return response.GET(gradService.graduateStudent(studentID,batchId,accessToken.replace(BEARER, ""),projectedType));
    }

    @GetMapping(EducGraduationApiConstants.GRADUATE_REPORT_DATA_BY_PEN)
    @PreAuthorize(PermissionsContants.GRADUATE_DATA)
    @Operation(summary = "Get Report data from graduation by student pen", description = "Get Report data from graduation by student pen", tags = { "Graduation Data" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<ReportData> reportDataByPen(@PathVariable @NotNull String pen, @RequestParam(required = false) String type,
                                                      @RequestHeader(name="Authorization") String accessToken) {
        LOGGER.debug("Report Data By Student Pen: {}", pen);
        return response.GET(gradService.prepareReportData(pen, type, accessToken.replace(BEARER, "")));
    }

    @GetMapping(EducGraduationApiConstants.GRADUATE_TRANSCRIPT_REPORT)
    @PreAuthorize(PermissionsContants.GRADUATE_TRANSCRIPT)
    @Operation(summary = "Get Transcript binary from graduation by student pen", description = "Get Transcript binary from graduation by student pen", tags = { "Graduation Data" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<byte[]> reportTranscriptByPen(@PathVariable @NotNull String pen, @RequestParam(required = false) String interim,
                                                      @RequestHeader(name="Authorization") String accessToken) {
        LOGGER.debug("Report Data By Student Pen: {}", pen);
        byte[] resultBinary = gradService.prepareTranscriptReport(pen, interim, accessToken.replace(BEARER, ""));
        if(resultBinary == null) {
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
                                                               @RequestParam(required = false) String type,
                                                               @RequestHeader(name="Authorization") String accessToken) {
        LOGGER.debug("Report Data from graduation for student: {}", graduationData.getGradStudent().getStudentID());
        return response.GET(gradService.prepareReportData(graduationData, type, accessToken.replace(BEARER, "")));
    }

    @PostMapping(EducGraduationApiConstants.SCHOOL_REPORTS)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "School Report Creation", description = "When triggered, School Reports are created", tags = { "Reports" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Integer> createAndStoreSchoolReports(@RequestBody List<String> uniqueSchools, @RequestHeader(name="Authorization") String accessToken,@RequestParam(required = false) String type ) {
        return response.GET(gradService.createAndStoreSchoolReports(uniqueSchools,type,accessToken.replace(BEARER, "")));
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
