package ca.bc.gov.educ.api.graduation.model.dto;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@Component
public class GradSpecialProgram extends BaseModel {

	private UUID id; 	
	private String specialProgramCode; 	
	private String specialProgramName; 	
	private String programCode;
	
	@Override
	public String toString() {
		return "GradSpecialProgram [id=" + id + ", specialProgramCode=" + specialProgramCode + ", specialProgramName="
				+ specialProgramName + ", programCode=" + programCode + "]";
	}
	
	
			
}
