package ca.bc.gov.educ.api.graduation.model.report;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class CertificateType {

    private String reportName;
    private PaperType paperType;

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String value) {
        this.reportName = value;
    }

    @JsonDeserialize(as = PaperType.class)
    public PaperType getPaperType() {
        return paperType;
    }

    public void setPaperType(PaperType value) {
        this.paperType = value;
    }
}
