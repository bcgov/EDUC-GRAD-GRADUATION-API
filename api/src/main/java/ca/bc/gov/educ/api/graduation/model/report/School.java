package ca.bc.gov.educ.api.graduation.model.report;

public class School {
    private String mincode;
    private String name;
    private String typeIndicator;
    private String typeBanner;
    private String signatureCode;
    private String distno;
    private String schlno;
    private String schoolCategoryCode;
    private Address address;

    public String getMincode() {
        return mincode;
    }

    public void setMincode(String value) {
        this.mincode = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getTypeIndicator() {
        return typeIndicator;
    }

    public void setTypeIndicator(String value) {
        this.typeIndicator = value;
    }

    public String getTypeBanner() {
        return typeBanner;
    }

    public void setTypeBanner(String value) {
        this.typeBanner = value;
    }

    public String getSignatureCode() {
        return signatureCode;
    }

    public void setSignatureCode(String value) {
        this.signatureCode = value;
    }

    public String getDistno() {
        return distno;
    }

    public void setDistno(String value) {
        this.distno = value;
    }

    public String getSchlno() {
        return schlno;
    }

    public void setSchlno(String value) {
        this.schlno = value;
    }

    public String getSchoolCategoryCode() {
        return schoolCategoryCode;
    }

    public void setSchoolCategoryCode(String value) {
        this.schoolCategoryCode = value;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address value) {
        this.address = value;
    }
}
