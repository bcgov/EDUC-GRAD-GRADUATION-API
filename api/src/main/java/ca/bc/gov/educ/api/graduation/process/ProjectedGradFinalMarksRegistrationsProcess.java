package ca.bc.gov.educ.api.graduation.process;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProjectedGradFinalMarksRegistrationsProcess extends BaseProcess {
	
	private static Logger logger = LoggerFactory.getLogger(ProjectedGradFinalMarksRegistrationsProcess.class);

	@Override
	public ProcessorData fire(ProcessorData processorData) {
		ExceptionMessage exception = new ExceptionMessage();
		long startTime = System.currentTimeMillis();
		logger.debug("************* TIME START  ************ {}",startTime);
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		GraduationStudentRecord gradResponse = processorData.getGradResponse();
		GraduationData graduationDataStatus = gradAlgorithmService.runProjectedAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), processorData.getAccessToken());
		if(algorithmSupport.checkForErrors(graduationDataStatus,algorithmResponse,processorData)){
			return processorData;
		}
		logger.debug("**** Grad Algorithm Completed: ****");
		//Code to prepare achievement report
		ProjectedRunClob projectedRunClob = ProjectedRunClob.builder()
				.graduated(graduationDataStatus.isGraduated())
				.nonGradReasons(graduationDataStatus.getNonGradReasons())
				.build();
		gradStatusService.saveStudentRecordProjectedRun(projectedRunClob, processorData.getStudentID(), processorData.getBatchId(), processorData.getAccessToken(), exception);
		gradResponse = gradStatusService.processProjectedResults(gradResponse, graduationDataStatus);
		List<StudentOptionalProgram> projectedOptionalGradResponse = optionalProgramService.projectedOptionalPrograms(graduationDataStatus, processorData.getStudentID(), processorData.getAccessToken());
		ReportData data = reportService.prepareAchievementReportData(graduationDataStatus, projectedOptionalGradResponse, processorData.getAccessToken(),exception);
		if (checkExceptions(data.getException(), algorithmResponse,processorData)) {
			return processorData;
		}
		ExceptionMessage excp = reportService.saveStudentAchivementReportJasper(gradResponse.getPen(), data, processorData.getAccessToken(), gradResponse.getStudentID(), exception, graduationDataStatus.isGraduated());
		if (checkExceptions(excp,algorithmResponse,processorData)) {
			logger.debug("**** Problem Generating TVR: ****");
			return processorData;
		}
		algorithmResponse.setStudentOptionalProgram(projectedOptionalGradResponse);
		algorithmResponse.setGraduationStudentRecord(gradResponse);
		long endTime = System.currentTimeMillis();
		long diff = (endTime - startTime)/1000;
		logger.debug("************* TIME Taken  ************ {}",diff);
		processorData.setAlgorithmResponse(algorithmResponse);
		return processorData;

	}
}
