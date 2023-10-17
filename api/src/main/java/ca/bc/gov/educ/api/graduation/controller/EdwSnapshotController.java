package ca.bc.gov.educ.api.graduation.controller;

import ca.bc.gov.educ.api.graduation.model.dto.EdwGraduationSnapshot;
import ca.bc.gov.educ.api.graduation.service.EdwSnapshotService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping(EducGraduationApiConstants.GRADUATION_API_ROOT_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for EDW Snapshot of Graduation Status", description = "This API is for EDW Snapshot of Graduation Status.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"GRAD_GRADUATE_STUDENT"})})
public class EdwSnapshotController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdwSnapshotController.class);
    private static final String BEARER = "Bearer ";

    @Autowired
    EdwSnapshotService edwSnapshotService;

    @Autowired
    GradValidation validation;

    @Autowired
    ResponseHelper response;

    @PostMapping(EducGraduationApiConstants.EDW_GRADUATION_SNAPSHOT)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    @Operation(summary = "Run a Graduation snapshot for EDW", description = "Run a Graduation snapshot for EDW", tags = { "EDW Snapshot" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<EdwGraduationSnapshot> snapshotGraduationStatus(@RequestBody EdwGraduationSnapshot snapshotRequest, @RequestHeader(name="Authorization") String accessToken) {
        LOGGER.debug("Snapshot Graduation Status for Student ID: {}", snapshotRequest.getStudentID());
        return response.GET(edwSnapshotService.processSnapshot(snapshotRequest, accessToken.replace(BEARER, "")));
    }

}
