package ca.bc.gov.educ.api.graduation.model.dto.institute;

import ca.bc.gov.educ.api.graduation.model.dto.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DistrictContact extends BaseModel implements Serializable {

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    private String districtContactId;
    private String districtId;
    private String districtContactTypeCode;
    private String phoneNumber;
    private String jobTitle;
    private String phoneExtension;
    private String alternatePhoneNumber;
    private String alternatePhoneExtension;
    private String email;
    private String firstName;
    private String lastName;
    private String effectiveDate;
    private String expiryDate;
}
