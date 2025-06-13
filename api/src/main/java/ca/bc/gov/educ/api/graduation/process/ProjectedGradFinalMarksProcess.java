package ca.bc.gov.educ.api.graduation.process;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Data
@Component
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProjectedGradFinalMarksProcess extends BaseProcess {
	
	@Override
	public ProcessorData fire(ProcessorData processorData) {
		long startTime = System.currentTimeMillis();
		log.debug("************* TIME START  ************ {}",startTime);
		ExceptionMessage exception = new ExceptionMessage();
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		GraduationStudentRecord gradResponse = processorData.getGradResponse();
		GraduationData graduationDataStatus = gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), exception);
		if(algorithmSupport.checkForErrors(graduationDataStatus,algorithmResponse,processorData)){
			return processorData;
		}
		log.debug("**** Grad Algorithm Completed: ****");
		gradResponse = gradStatusService.processProjectedResults(gradResponse,graduationDataStatus);
		List<StudentOptionalProgram> projectedOptionalGradResponse = optionalProgramService.projectedOptionalPrograms(graduationDataStatus, processorData.getStudentID());
		algorithmResponse.setStudentOptionalProgram(projectedOptionalGradResponse);
		algorithmResponse.setGraduationStudentRecord(gradResponse);

		long endTime = System.currentTimeMillis();
		long diff = (endTime - startTime)/1000;
		log.debug("************* TIME Taken  ************ {} secs",diff);
		processorData.setAlgorithmResponse(algorithmResponse);
		return processorData;
	}
}
