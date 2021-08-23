package ca.bc.gov.educ.api.graduation.model.report;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public class ReportOptions {
    private boolean cacheReport;
    private String convertTo;
    private boolean overwrite;
    private String reportName;
    private String reportFile;

    public boolean getCacheReport() {
        return cacheReport;
    }

    public void setCacheReport(boolean value) {
        this.cacheReport = value;
    }

    public String getConvertTo() {
        return convertTo;
    }

    public void setConvertTo(String value) {
        this.convertTo = value;
    }

    public boolean getOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean value) {
        this.overwrite = value;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String value) {
        this.reportName = value;
    }

    public String getReportFile() {
        return reportFile;
    }

    public void setReportFile(String value) {
        this.reportFile = value;
    }
}
