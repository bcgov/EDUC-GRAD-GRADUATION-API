package ca.bc.gov.educ.api.graduation.process;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Data
@Component
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProjectedGradFinalMarksRegistrationsProcess extends BaseProcess {
	
	@Override
	public ProcessorData fire(ProcessorData processorData) {
		ExceptionMessage exception = new ExceptionMessage();
		long startTime = System.currentTimeMillis();
		log.debug("************* TIME START  ************ {}",startTime);
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		GraduationStudentRecord gradResponse = processorData.getGradResponse();
		GraduationData graduationDataStatus = gradAlgorithmService.runProjectedAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram());
		if(algorithmSupport.checkForErrors(graduationDataStatus,algorithmResponse,processorData)){
			return processorData;
		}
		log.debug("**** Grad Algorithm Completed: ****");
		//Code to prepare achievement report
		ProjectedRunClob projectedRunClob = ProjectedRunClob.builder()
				.graduated(graduationDataStatus.isGraduated())
				.nonGradReasons(graduationDataStatus.getNonGradReasons())
				.build();
		gradStatusService.saveStudentRecordProjectedRun(projectedRunClob, processorData.getStudentID(), processorData.getBatchId(), exception);
		gradResponse = gradStatusService.processProjectedResults(gradResponse, graduationDataStatus);
		List<StudentOptionalProgram> projectedOptionalGradResponse = optionalProgramService.projectedOptionalPrograms(graduationDataStatus, processorData.getStudentID());
		ReportData data = reportService.prepareAchievementReportData(UUID.fromString(processorData.getStudentID()), graduationDataStatus, projectedOptionalGradResponse, exception);
		if (checkExceptions(data.getException(), algorithmResponse,processorData)) {
			return processorData;
		}
		ExceptionMessage excp = reportService.saveStudentAchivementReportJasper(gradResponse.getPen(), data, gradResponse.getStudentID(), exception, graduationDataStatus.isGraduated());
		if (checkExceptions(excp,algorithmResponse,processorData)) {
			log.debug("**** Problem Generating TVR: ****");
			return processorData;
		}
		algorithmResponse.setStudentOptionalProgram(projectedOptionalGradResponse);
		algorithmResponse.setGraduationStudentRecord(gradResponse);
		long endTime = System.currentTimeMillis();
		long diff = (endTime - startTime)/1000;
		log.debug("************* TIME Taken  ************ {}",diff);
		processorData.setAlgorithmResponse(algorithmResponse);
		return processorData;

	}
}
