package ca.bc.gov.educ.api.graduation.model.dto.institute;

import ca.bc.gov.educ.api.graduation.model.dto.BaseModel;
import lombok.*;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Component
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class District extends BaseModel implements Serializable {
    private static final long serialVersionUID = 2L;

    private String districtId;
    private String districtNumber;
    private String faxNumber;
    private String phoneNumber;
    private String email;
    private String website;
    private String displayName;
    private String districtRegionCode;
    private String districtStatusCode;
    private List<DistrictContact> contacts;
    private List<DistrictAddress> addresses;

    public String getDistrictName() {
        return displayName;
    }
}
