package ca.bc.gov.educ.api.graduation.model.report;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class OrderType {

    private String name;
    private CertificateType certificateType;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    @JsonDeserialize(as = CertificateType.class)
    public CertificateType getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(CertificateType value) {
        this.certificateType = value;
    }
}
