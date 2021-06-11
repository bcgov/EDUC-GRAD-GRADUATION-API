package ca.bc.gov.educ.api.graduation.model.dto;

import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessorData {

	private GraduationStatus gradResponse;
	private AlgorithmResponse algorithmResponse;
	private String accessToken;
	private String studentID;
}
