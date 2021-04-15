package ca.bc.gov.educ.api.graduation.model.dto;

import java.util.UUID;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class GradStudentCertificates extends BaseModel{

	private UUID id;	
	private String pen;	
	private String certificate;
	private String gradCertificateTypeCode;
	private String gradCertificateTypeDesc;
	private UUID studentID;
}
