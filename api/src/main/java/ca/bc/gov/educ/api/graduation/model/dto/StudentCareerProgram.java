package ca.bc.gov.educ.api.graduation.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentCareerProgram {

	private UUID id;	
	private String careerProgramCode;	
	private String careerProgramName;
	private UUID studentID;
	
}
