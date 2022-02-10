package ca.bc.gov.educ.api.graduation.process;

import java.util.ArrayList;
import java.util.List;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.service.GradAlgorithmService;
import ca.bc.gov.educ.api.graduation.service.GradStatusService;
import ca.bc.gov.educ.api.graduation.service.OptionalProgramService;
import ca.bc.gov.educ.api.graduation.service.ReportService;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class ProjectedGradFinalMarksReportsProcess implements AlgorithmProcess {
	
	private static Logger logger = LoggerFactory.getLogger(ProjectedGradFinalMarksReportsProcess.class);
	
	@Autowired
    private ProcessorData processorData;
    
	@Autowired
	GradStatusService gradStatusService;
	
	@Autowired
	GradAlgorithmService gradAlgorithmService;
	
	@Autowired
	OptionalProgramService optionalProgramService;

	@Autowired
	ReportService reportService;
	
	@Autowired
	GradValidation validation;

	@Autowired
	AlgorithmSupport algorithmSupport;
	
	@Override
	public ProcessorData fire() {
		long startTime = System.currentTimeMillis();
		logger.info("************* TIME START  ************ {}",startTime);
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		GraduationStudentRecord gradResponse = processorData.getGradResponse();
		ExceptionMessage exception = new ExceptionMessage();
		if(gradResponse.getProgramCompletionDate() != null) {
			List<CodeDTO> optionalProgram = new ArrayList<>();
			GraduationData graduationDataStatus = gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), processorData.getAccessToken(),exception);

			if(algorithmSupport.checkForErrors(graduationDataStatus,algorithmResponse,processorData)){
				return processorData;
			}
			logger.info("**** Grad Algorithm Completed: ****");
			List<StudentOptionalProgram> projectedOptionalGradResponse = optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,processorData.getStudentID(),processorData.getAccessToken(),optionalProgram);
			logger.info("**** Saved Optional Programs: ****");
			GraduationStudentRecord toBeSaved = gradStatusService.prepareGraduationStatusObj(graduationDataStatus);
			ReportData data = reportService.prepareReportData(graduationDataStatus,gradResponse,processorData.getAccessToken(),exception);
			logger.info("**** Prepared Data for Reports: ****");
			if(toBeSaved != null && toBeSaved.getStudentID() != null) {
				GraduationStudentRecord graduationStatusResponse = gradStatusService.saveStudentGradStatus(processorData.getStudentID(),processorData.getBatchId(), processorData.getAccessToken(),toBeSaved,exception);
				logger.info("**** Saved Grad Status: ****");
				algorithmSupport.createReportNCert(graduationDataStatus,graduationStatusResponse,gradResponse,projectedOptionalGradResponse,exception,data,processorData);
				if(exception.getExceptionName() != null) {
					algorithmResponse.setException(exception);
					processorData.setAlgorithmResponse(algorithmResponse);
					gradStatusService.restoreStudentGradStatus(processorData.getStudentID(), processorData.getAccessToken(),graduationDataStatus != null && graduationDataStatus.isGraduated());
					logger.info("**** Record Restored Due to Error: ****");
					return processorData;
				}
				algorithmResponse.setGraduationStudentRecord(graduationStatusResponse);
				algorithmResponse.setStudentOptionalProgram(projectedOptionalGradResponse);
			}
		}else {
			exception.setExceptionName("STUDENT-NOT-GRADUATED-YET");
			exception.setExceptionDetails("Graduation Algorithm Cannot be Run for this graduated Student");
			algorithmResponse.setException(exception);
		}
		long endTime = System.currentTimeMillis();
		long diff = (endTime - startTime)/1000;
		logger.info("************* TIME Taken  ************ {} secs",diff);
		processorData.setAlgorithmResponse(algorithmResponse);
		return processorData;
	}


	@Override
    public void setInputData(ProcessorData inputData) {
		processorData = inputData;
        logger.info("ProjectedGradFinalMarksReportsProcess: ");
    }

}
