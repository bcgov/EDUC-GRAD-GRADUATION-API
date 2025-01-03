package ca.bc.gov.educ.api.graduation.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
public class SchoolReports extends BaseModel {

	private UUID id;
	private String report;
	private String reportTypeCode;
	private String reportTypeLabel;
	private String schoolOfRecord;
	private UUID schoolOfRecordId;
}
