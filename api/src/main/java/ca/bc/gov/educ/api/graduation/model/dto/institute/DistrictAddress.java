package ca.bc.gov.educ.api.graduation.model.dto.institute;

import ca.bc.gov.educ.api.graduation.model.dto.BaseModel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class DistrictAddress extends BaseModel implements Serializable {
    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    private String districtAddressId;
    private String districtId;
    private String addressTypeCode;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String postal;
    private String provinceCode;
    private String countryCode;
}