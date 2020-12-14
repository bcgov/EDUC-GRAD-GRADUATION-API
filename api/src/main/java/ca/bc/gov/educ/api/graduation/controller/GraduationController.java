package ca.bc.gov.educ.api.graduation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.service.GraduationService;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.PermissionsContants;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@CrossOrigin
@RestController
@RequestMapping(EducGraduationApiConstants.GRADUATION_API_ROOT_MAPPING)
@EnableResourceServer
@OpenAPIDefinition(info = @Info(title = "API for Graduating Student.", description = "This Read API is for Graduating Student.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"GRAD_GRADUATE_STUDENT"})})
public class GraduationController {

	private static Logger logger = LoggerFactory.getLogger(GraduationController.class);

    @Autowired
    GraduationService gradService;

    @GetMapping (EducGraduationApiConstants.GRADUATE_STUDENT_BY_PEN)
    @PreAuthorize(PermissionsContants.GRADUATE_STUDENT)
    public GraduationStatus graduateStudent(@PathVariable String pen) {
        logger.debug("Graduate Student for PEN: " + pen);
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        return gradService.graduateStudentByPen(pen,accessToken);
    }    
   
}
