package ca.bc.gov.educ.api.graduation.model.dto;

import ca.bc.gov.educ.api.graduation.util.GradLocalDateTimeDeserializer;
import ca.bc.gov.educ.api.graduation.util.GradLocalDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseModel {
	private String createUser;
	@JsonSerialize(using = GradLocalDateTimeSerializer.class)
	@JsonDeserialize(using = GradLocalDateTimeDeserializer.class)
	private LocalDateTime createDate;
	private String updateUser;
	@JsonSerialize(using = GradLocalDateTimeSerializer.class)
	@JsonDeserialize(using = GradLocalDateTimeDeserializer.class)
	private LocalDateTime updateDate;
}
