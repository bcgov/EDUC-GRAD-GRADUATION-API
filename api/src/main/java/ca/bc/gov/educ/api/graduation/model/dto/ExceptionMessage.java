package ca.bc.gov.educ.api.graduation.model.dto;

import org.springframework.stereotype.Component;

import lombok.Data;

import java.io.Serializable;

@Data
@Component
public class ExceptionMessage {

	private String exceptionName;
	private String exceptionDetails;
}
