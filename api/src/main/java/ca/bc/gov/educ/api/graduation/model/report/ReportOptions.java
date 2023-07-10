package ca.bc.gov.educ.api.graduation.model.report;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public class ReportOptions implements Serializable {

	private static final long serialVersionUID = 2L;

	private boolean cacheReport;
	private String convertTo;
	private boolean overwrite;
	private boolean preview;
	private String reportName;
	private String reportFile;

}
