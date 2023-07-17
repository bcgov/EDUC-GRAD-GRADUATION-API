package ca.bc.gov.educ.api.graduation.model.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BaseModel {
	private String createUser;	
	private LocalDate createDate;
	private String updateUser;	
	private LocalDate updateDate;
}
