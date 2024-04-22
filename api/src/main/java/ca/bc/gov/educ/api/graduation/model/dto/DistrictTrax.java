package ca.bc.gov.educ.api.graduation.model.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class DistrictTrax {

    private String districtNumber;
    private String districtName;
    private String districtSeq;
    private String schoolETPSystem;
    private String superIntendent;
    private String djdeFlash;
    private String activeFlag;
    private String address1;
    private String address2;
    private String city;
    private String provCode;
    private String countryCode;
    private String postal;

    public String getDistrictName() {
        return districtName != null ? districtName.trim(): null;
    }

    public String getDistrictSeq() {  return districtSeq != null ? districtSeq.trim(): null; }

    public String getSchoolETPSystem() { return schoolETPSystem != null ? schoolETPSystem.trim(): null; }

    public String getSuperIntendent() {return superIntendent != null ? superIntendent.trim(): null;}

    public String getDjdeFlash() {        return djdeFlash != null ? djdeFlash.trim(): null;    }

    public String getActiveFlag() {        return activeFlag != null ? activeFlag.trim(): null;    }

    public String getAddress1() {        return address1 != null ? address1.trim(): null;    }

    public String getAddress2() {        return address2 != null ? address2.trim(): null;    }

    public String getCity() {        return postal != null ? city.trim(): null;    }

    public String getPostal() {        return postal != null ? postal.trim(): null;    }

    @Override
    public String toString() {
        return "District [districtNumber=" + districtNumber + ", districtName=" + districtName + ", districtSeq="
                + districtSeq + ", schoolETPSystem=" + schoolETPSystem + ", superIntendent=" + superIntendent
                + ", djdeFlash=" + djdeFlash + ", activeFlag=" + activeFlag + ", address1=" + address1 + ", address2="
                + address2 + ", city=" + city + ", provCode=" + provCode + ", countryCode=" + countryCode + ", postal="
                + postal + "]";
    }


}

