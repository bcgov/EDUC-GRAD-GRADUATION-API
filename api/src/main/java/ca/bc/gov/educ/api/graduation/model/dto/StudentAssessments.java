package ca.bc.gov.educ.api.graduation.model.dto;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class StudentAssessments {

	private String assessmentCode="LTE10";
	private String assessmentName="LITERACY ASSESSMENT 10";
	private String sessionDate="2019/06";
	private String gradReqMet="15";
	private String proficiencyScore="0.0";
}
