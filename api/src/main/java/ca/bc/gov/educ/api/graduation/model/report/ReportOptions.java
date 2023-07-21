package ca.bc.gov.educ.api.graduation.model.report;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ReportOptions implements Serializable {

	private static final long serialVersionUID = 2L;

	private boolean cacheReport;
	private String convertTo;
	private boolean overwrite;
	private boolean preview;
	private String reportName;
	private String reportFile;

}
