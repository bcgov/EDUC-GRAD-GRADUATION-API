package ca.bc.gov.educ.api.graduation.process;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@Component
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GraduateStudentProcess extends BaseProcess {
	
	@Override
	public ProcessorData fire(ProcessorData processorData) {
		long startTime = System.currentTimeMillis();
		log.debug("************* TIME START  ************ {}",startTime);
		ExceptionMessage exception = new ExceptionMessage();
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		GraduationStudentRecord gradResponse = processorData.getGradResponse();
		if (gradResponse.getProgramCompletionDate() == null || gradResponse.getProgram().equalsIgnoreCase("SCCP") || gradResponse.getProgram().equalsIgnoreCase("NOPROG")) {
			List<CodeDTO> optionalProgram = new ArrayList<>();
			GraduationData graduationDataStatus = gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), exception);
			if(algorithmSupport.checkForErrors(graduationDataStatus,algorithmResponse,processorData)){
				return processorData;
			}
			log.debug("**** Grad Algorithm Completed:{} **** ",gradResponse.getStudentID());

			List<StudentOptionalProgram> projectedOptionalGradResponse = optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus, processorData.getStudentID(), optionalProgram);
			log.debug("**** Saved Optional Programs: ****");
			GraduationStudentRecord toBeSaved = gradStatusService.prepareGraduationStatusObj(graduationDataStatus);
			toBeSaved.setUpdateUser(StringUtils.isEmpty(gradResponse.getUpdateUser()) ? "" : gradResponse.getUpdateUser());
			if(checkExceptions(exception,algorithmResponse,processorData)) {
				return processorData;
			}
			log.debug("**** Prepared Data to Save Grad Status: ****");
			if (toBeSaved != null && toBeSaved.getStudentID() != null) {
				GraduationStudentRecord graduationStatusResponse = gradStatusService.saveStudentGradStatus(processorData.getStudentID(), processorData.getBatchId(), toBeSaved, exception);
				if (checkExceptions(graduationStatusResponse.getException(),algorithmResponse,processorData)) {
					return processorData;
				}
				log.debug("**** Saved Grad Status: ****");
				ReportData data = reportService.prepareTranscriptData(graduationDataStatus, gradResponse, false, exception);
				if(checkExceptions(data.getException(),algorithmResponse,processorData)) {
					return processorData;
				}
				log.debug("**** Prepared Data for Reports: ****");
				ExceptionMessage eMsg = algorithmSupport.createStudentCertificateTranscriptReports(graduationDataStatus,graduationStatusResponse,gradResponse,projectedOptionalGradResponse,exception,data,processorData, "GS");
				if (checkExceptions(eMsg,algorithmResponse,processorData)) {
					gradStatusService.restoreStudentGradStatus(processorData.getStudentID(), graduationDataStatus.isGraduated());
					log.debug("**** Record Restored Due to Error: ****");
					return processorData;
				}
				log.debug("**** Saved Grad Status: ****");
				algorithmResponse.setGraduationStudentRecord(graduationStatusResponse);
				algorithmResponse.setStudentOptionalProgram(projectedOptionalGradResponse);
			}
		} else {
			exception.setExceptionName("STUDENT-ALREADY-GRADUATED");
			exception.setExceptionDetails("Graduation Algorithm Cannot be Run for this graduated Student");
			algorithmResponse.setException(exception);
		}
		long endTime = System.currentTimeMillis();
		long diff = (endTime - startTime)/1000;
		log.debug("************* TIME Taken  ************ {} secs",diff);
		processorData.setAlgorithmResponse(algorithmResponse);
		return processorData;
	}
}
