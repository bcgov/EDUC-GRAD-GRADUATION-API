package ca.bc.gov.educ.api.graduation.model.dto;

import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GradStudentSpecialProgram extends BaseModel{

	private UUID id;
    private String pen;
    private UUID specialProgramID;
    private String studentSpecialProgramData;
    private String specialProgramCompletionDate;
    private String specialProgramName;
    private String specialProgramCode;
    private String mainProgramCode;
				
}
