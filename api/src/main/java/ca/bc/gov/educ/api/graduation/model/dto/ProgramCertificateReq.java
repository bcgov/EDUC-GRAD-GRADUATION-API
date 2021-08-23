package ca.bc.gov.educ.api.graduation.model.dto;

import lombok.Data;

@Data
public class ProgramCertificateReq {

	private String optionalProgram;	
	private String programCode;	
	private String schoolFundingCode;
}
