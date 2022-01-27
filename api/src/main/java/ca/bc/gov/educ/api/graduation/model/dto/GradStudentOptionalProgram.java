package ca.bc.gov.educ.api.graduation.model.dto;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GradStudentOptionalProgram extends BaseModel{

	private UUID id;
    private String pen;
    private UUID optionalProgramID;
    private String studentOptionalProgramData;
    private String optionalProgramCompletionDate;
    private String optionalProgramName;
    private String optionalProgramCode;
    private String mainProgramCode;
    private UUID studentID;
				
}
