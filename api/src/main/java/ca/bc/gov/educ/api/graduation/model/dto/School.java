package ca.bc.gov.educ.api.graduation.model.dto;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class School {

    private String minCode;
    private String schoolName;
    private String districtName;
    private String transcriptEligibility;
    private String certificateEligibility;
    private String independentDesignation;
    private String mailerType;
    private String address1;
    private String address2;
    private String city;
    private String provCode;
    private String provinceName;
    private String countryCode;
    private String countryName;
    private String postal;
    private String independentAffiliation;

    @Override
    public String toString() {
        return "School [minCode=" + minCode + ", schoolName=" + schoolName + ", districtName=" + districtName
                + ", transcriptEligibility=" + transcriptEligibility + ", certificateEligibility="
                + certificateEligibility + ", independentDesignation=" + independentDesignation + ", mailerType="
                + mailerType + ", address1=" + address1 + ", address2=" + address2 + ", city=" + city + ", provCode="
                + provCode + ", provinceName=" + provinceName + ", countryCode=" + countryCode + ", countryName="
                + countryName + ", postal=" + postal + "]";
    }
}
