package ca.bc.gov.educ.api.graduation.process;

import java.util.List;

import ca.bc.gov.educ.api.graduation.model.achvreport.AchvReportData;
import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.graduation.service.GradAlgorithmService;
import ca.bc.gov.educ.api.graduation.service.GradStatusService;
import ca.bc.gov.educ.api.graduation.service.OptionalProgramService;
import ca.bc.gov.educ.api.graduation.util.GradBusinessRuleException;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
public class ProjectedGradFinalMarksRegistrationsProcess implements AlgorithmProcess {
	
	private static Logger logger = LoggerFactory.getLogger(ProjectedGradFinalMarksRegistrationsProcess.class);
	
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

	
	@Override
	public ProcessorData fire() {
		ExceptionMessage exception = processorData.getException();
		long startTime = System.currentTimeMillis();
		logger.info("************* TIME START  ************ "+startTime);
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		GraduationStudentRecord gradResponse = processorData.getGradResponse();
		if(!gradResponse.getProgram().equalsIgnoreCase("SCCP") && !gradResponse.getProgram().equalsIgnoreCase("NOPROG")) {
			GraduationData graduationDataStatus = gradAlgorithmService.runProjectedAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), processorData.getAccessToken());
			if (graduationDataStatus != null && graduationDataStatus.getException() != null && graduationDataStatus.getException().getExceptionName() != null) {
				logger.info("**** Grad Algorithm Has Errors: ****");
				algorithmResponse.setException(graduationDataStatus.getException());
				processorData.setAlgorithmResponse(algorithmResponse);
				return processorData;
			} else if (exception.getExceptionName() != null) {
				logger.info("**** Grad Algorithm errored out: ****");
				algorithmResponse.setException(exception);
				processorData.setAlgorithmResponse(algorithmResponse);
				return processorData;
			}
			logger.info("**** Grad Algorithm Completed: ****");
			//Code to prepare achievement report
			GraduationStudentRecord graduationStatusResponse = gradStatusService.saveStudentRecordProjectedRun(processorData.getStudentID(), processorData.getBatchId(), processorData.getAccessToken(), exception);
			gradResponse = gradStatusService.processProjectedResults(graduationStatusResponse, graduationDataStatus);
			List<StudentOptionalProgram> projectedOptionalGradResponse = optionalProgramService.projectedOptionalPrograms(graduationDataStatus, processorData.getStudentID(), processorData.getAccessToken());
			AchvReportData data = reportService.prepareAchievementReportData(graduationDataStatus, projectedOptionalGradResponse);
			reportService.saveStudentAchivementReportJasper(graduationStatusResponse.getPen(), data, processorData.getAccessToken(), graduationStatusResponse.getStudentID(), exception, graduationDataStatus.isGraduated());
			algorithmResponse.setStudentOptionalProgram(projectedOptionalGradResponse);
			algorithmResponse.setGraduationStudentRecord(gradResponse);
		}else {
			exception.setExceptionName("PROJECTED_RUN_NOT_ALLOWED");
			exception.setExceptionDetails("Graduation Projected Algorithm Cannot be Run for this graduated Student");
			algorithmResponse.setException(exception);
		}
		long endTime = System.currentTimeMillis();
		long diff = (endTime - startTime)/1000;
		logger.info("************* TIME Taken  ************ "+diff+" secs");
		processorData.setAlgorithmResponse(algorithmResponse);
		return processorData;

	}

	@Override
    public void setInputData(ProcessorData inputData) {
		processorData = (ProcessorData)inputData;
        logger.info("ProjectedGradFinalMarksRegistraionProcess: ");
    }

}
