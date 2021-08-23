package ca.bc.gov.educ.api.graduation.model.dto;

import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ProgramCertificate extends BaseModel {

	private UUID pcId;
	private String label;
	private String schoolFundingGroupCode;
	private String certificateTypeCode;
	private String mediaCode;
}
