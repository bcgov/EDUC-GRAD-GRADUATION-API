package ca.bc.gov.educ.api.graduation.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseModel {
	private String createUser;	
	private LocalDateTime createDate;
	private String updateUser;	
	private LocalDateTime updateDate;
}
