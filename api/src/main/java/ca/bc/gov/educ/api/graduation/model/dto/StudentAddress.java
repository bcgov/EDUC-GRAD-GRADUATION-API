package ca.bc.gov.educ.api.graduation.model.dto;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class StudentAddress {

	private String address1;
	private String address2;
	private String city;
	private String postalCode;
	private String provinceName;
	private String contryName;
}
