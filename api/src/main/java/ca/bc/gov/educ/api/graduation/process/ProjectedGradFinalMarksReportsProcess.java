package ca.bc.gov.educ.api.graduation.process;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProjectedGradFinalMarksReportsProcess extends BaseProcess{
	
	private static Logger logger = LoggerFactory.getLogger(ProjectedGradFinalMarksReportsProcess.class);
	
	@Override
	public ProcessorData fire(ProcessorData processorData) {
		long startTime = System.currentTimeMillis();
		logger.debug("************* TIME START  ************ {}",startTime);
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		GraduationStudentRecord gradResponse = processorData.getGradResponse();
		ExceptionMessage exception = new ExceptionMessage();
		if(gradResponse.getProgramCompletionDate() != null) {
			List<CodeDTO> optionalProgram = new ArrayList<>();
			GraduationData graduationDataStatus = gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), processorData.getAccessToken(), exception);
			if(algorithmSupport.checkForErrors(graduationDataStatus,algorithmResponse,processorData)){
				return processorData;
			}
			logger.debug("**** Grad Algorithm Completed:{} **** ",gradResponse.getStudentID());

			List<StudentOptionalProgram> projectedOptionalGradResponse = optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus, processorData.getStudentID(), processorData.getAccessToken(), optionalProgram);
			logger.debug("**** Saved Optional Programs: ****");
			GraduationStudentRecord toBeSaved = gradStatusService.prepareGraduationStatusObj(graduationDataStatus);
			ReportData data = reportService.prepareTranscriptData(graduationDataStatus,gradResponse,false,processorData.getAccessToken(),exception);
			if(checkExceptions(data.getException(),algorithmResponse,processorData)) {
				return processorData;
			}
			logger.debug("**** Prepared Data for Reports: ****");
			if (toBeSaved != null && toBeSaved.getStudentID() != null) {
				GraduationStudentRecord graduationStatusResponse = gradStatusService.saveStudentGradStatus(processorData.getStudentID(), processorData.getBatchId(), processorData.getAccessToken(), toBeSaved, exception);
				if (checkExceptions(graduationStatusResponse.getException(),algorithmResponse,processorData)) {
					return processorData;
				}
				logger.debug("**** Saved Grad Status: ****");
				ExceptionMessage eMsg = algorithmSupport.createStudentCertificateTranscriptReports(graduationDataStatus,graduationStatusResponse,gradResponse,projectedOptionalGradResponse,exception,data,processorData, "FMR");
				if (checkExceptions(eMsg,algorithmResponse,processorData)) {
					gradStatusService.restoreStudentGradStatus(processorData.getStudentID(), processorData.getAccessToken(), graduationDataStatus.isGraduated());
					logger.debug("**** Record Restored Due to Error: ****");
					return processorData;
				}
				logger.debug("**** Saved Grad Status: ****");
				algorithmResponse.setGraduationStudentRecord(graduationStatusResponse);
				algorithmResponse.setStudentOptionalProgram(projectedOptionalGradResponse);
			}
		} else {
			exception.setExceptionName("STUDENT-NOT-GRADUATED-YET");
			exception.setExceptionDetails("Graduation Algorithm Cannot be Run for this graduated Student");
			algorithmResponse.setException(exception);
		}
		long endTime = System.currentTimeMillis();
		long diff = (endTime - startTime)/1000;
		logger.debug("************* TIME Taken  ************ {} secs",diff);
		processorData.setAlgorithmResponse(algorithmResponse);
		return processorData;
	}
}
