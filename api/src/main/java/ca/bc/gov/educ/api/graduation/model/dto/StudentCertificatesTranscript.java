package ca.bc.gov.educ.api.graduation.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class StudentCertificatesTranscript implements Serializable {

	private List<String> certificateTypeCodes = new ArrayList<>();
	private String transcriptTypeCode;

	public List<String> getCertificateTypeCodes() {
		if(this.certificateTypeCodes == null) {
			this.certificateTypeCodes = new ArrayList<>();
		}
		return this.certificateTypeCodes;
	}

	public void addCertificateTypeCode(String code) {
		getCertificateTypeCodes().add(code);
	}
}
