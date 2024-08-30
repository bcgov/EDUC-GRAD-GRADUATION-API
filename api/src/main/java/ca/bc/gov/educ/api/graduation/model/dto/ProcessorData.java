package ca.bc.gov.educ.api.graduation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessorData {

	private GraduationStudentRecord gradResponse;
	private AlgorithmResponse algorithmResponse;
//	private String accessToken;
	private String studentID;
	private Long batchId;
	private ExceptionMessage exception;

	private long startTime;

	public ProcessorData(GraduationStudentRecord gradResponse, AlgorithmResponse algorithmResponse, String studentID, Long batchId, ExceptionMessage exception) {
		this.gradResponse = gradResponse;
		this.algorithmResponse = algorithmResponse;
		this.studentID = studentID;
		this.batchId = batchId;
		this.exception = exception;
		this.startTime = System.currentTimeMillis();
	}

}
