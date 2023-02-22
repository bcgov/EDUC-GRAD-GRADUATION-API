package ca.bc.gov.educ.api.graduation.model.dto;

import lombok.*;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Component
public class GradCertificateType {

	private String code;
	private String label;
	private String description;
	
	@Override
	public String toString() {
		return "GradCertificateTypes [code=" + code + ", label=" + label + ", description=" + description + "]";
	}
	
	
}
