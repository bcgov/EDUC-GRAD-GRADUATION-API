package ca.bc.gov.educ.api.graduation.model.dto;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class GraduationMessages {
	private String gradProgram;
	private String gradMessage;
	private String honours;
	private String gpa;
	private boolean hasSpecialProgram;
	private boolean hasCareerProgram;
	private boolean hasCertificates;
	private List<CodeDTO> specialProgram;
	private List<CodeDTO> careerProgram;
	private List<CodeDTO> certificateProgram;
	private List<GradRequirement> nonGradReasons;
}
