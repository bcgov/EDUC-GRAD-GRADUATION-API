package ca.bc.gov.educ.api.graduation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = true)
@Component
@NoArgsConstructor
@AllArgsConstructor
public class District extends BaseModel {

    private String districtId;
    private String districtNumber;
    private String faxNumber;
    private String phoneNumber;
    private String email;
    private String website;
    private String displayName;
    private String districtRegionCode;
    private String districtStatusCode;

    public String getDistrictName() {
        return displayName;
    }
}
