package ca.bc.gov.educ.api.graduation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.*;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
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

@CrossOrigin
@RestController
@RequestMapping(EducGraduationApiConstants.GRADUATION_API_ROOT_MAPPING)
@EnableResourceServer
@OpenAPIDefinition(info = @Info(title = "API for Graduating Student.", description = "This API is for Graduating Student.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"GRAD_GRADUATE_STUDENT"})})
public class GraduationController {

    private static Logger logger = LoggerFactory.getLogger(GraduationController.class);

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
    public ResponseEntity<AlgorithmResponse> graduateStudentNew(@PathVariable String studentID, @PathVariable String projectedType, @RequestParam(required = false) Long batchId) {
        logger.debug("Graduate Student for Student ID: " + studentID);
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
        String accessToken = auth.getTokenValue();
        return response.GET(gradService.graduateStudent(studentID,batchId,accessToken,projectedType));
    }

}
