package ca.bc.gov.educ.api.graduation.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProgramCertificateTranscript extends BaseModel {

	private UUID pcId;
	private String graduationProgramCode;
	private String schoolCategoryCode;
	private String certificateTypeCode;
	private String certificateTypeLabel;
	private String transcriptTypeCode;
	private String transcriptTypeLabel;
	private String transcriptPaperType;
	private String certificatePaperType;
}
