package ca.bc.gov.educ.api.graduation.model.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonPropertyOrder({ "options", "data"})
public class ReportRequest {
    private ReportOptions options;
    private ReportData reportData;

    @JsonProperty("options")
    public ReportOptions getOptions() { return options; }
    public void setOptions(ReportOptions value) { this.options = value; }

    @JsonProperty("data")
    public ReportData getData() { return reportData; }
    public void setData(ReportData value) { this.reportData = value; }
}


