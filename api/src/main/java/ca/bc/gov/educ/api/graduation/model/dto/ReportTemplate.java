package ca.bc.gov.educ.api.graduation.model.dto;

import lombok.Data;

@Data
public class ReportTemplate {

	private String encodingType="base64";
	private String fileType="docx";
	private String content;
    
}
