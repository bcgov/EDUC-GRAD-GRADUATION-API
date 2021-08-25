package ca.bc.gov.educ.api.graduation.model.dto;

import java.sql.Date;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@Component
public class SpecialCase extends BaseModel {

	private String spCase;	
	private String label;	
	private int displayOrder; 
	private String description;
	private String passFlag;
	private Date effectiveDate; 
	private Date expiryDate;
	
	@Override
	public String toString() {
		return "SpecialCase [spCase=" + spCase + ", label=" + label + ", displayOrder=" + displayOrder
				+ ", description=" + description + ", passFlag=" + passFlag + ", effectiveDate=" + effectiveDate
				+ ", expiryDate=" + expiryDate + "]";
	}
	
	
}
