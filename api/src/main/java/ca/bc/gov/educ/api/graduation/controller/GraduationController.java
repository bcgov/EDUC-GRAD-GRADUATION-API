package ca.bc.gov.educ.api.graduation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.service.GraduationService;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;

@CrossOrigin
@RestController
@RequestMapping(EducGraduationApiConstants.GRADUATION_API_ROOT_MAPPING)
public class GraduationController {

	private static Logger logger = LoggerFactory.getLogger(GraduationController.class);

    @Autowired
    GraduationService gradService;

    @GetMapping (EducGraduationApiConstants.GRADUATE_STUDENT_BY_PEN)
    public GraduationStatus graduateStudent(@PathVariable String pen) {
        logger.debug("Graduate Student for PEN: " + pen);
        return gradService.graduateStudentByPen(pen);
    }    
   
}
