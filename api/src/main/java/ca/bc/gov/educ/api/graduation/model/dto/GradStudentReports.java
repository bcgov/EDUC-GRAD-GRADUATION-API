package ca.bc.gov.educ.api.graduation.model.dto;

import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class GradStudentReports extends BaseModel{

	private UUID id;	
	private String pen;	
	private String report;
	private String gradReportTypeCode;
}
