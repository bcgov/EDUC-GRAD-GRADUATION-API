package ca.bc.gov.educ.api.graduation.process;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.CodeDTO;
import ca.bc.gov.educ.api.graduation.model.dto.ExceptionMessage;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.graduation.model.dto.ProcessorData;
import ca.bc.gov.educ.api.graduation.model.dto.ProgramCertificate;
import ca.bc.gov.educ.api.graduation.model.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.service.GradAlgorithmService;
import ca.bc.gov.educ.api.graduation.service.GradStatusService;
import ca.bc.gov.educ.api.graduation.service.OptionalProgramService;
import ca.bc.gov.educ.api.graduation.service.ReportService;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Component
@NoArgsConstructor
public class GraduateStudentProcess implements AlgorithmProcess {
	
	private static Logger logger = LoggerFactory.getLogger(GraduateStudentProcess.class);
	
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
	
	
	@Override
	public ProcessorData fire() {				
		long startTime = System.currentTimeMillis();
		logger.info("************* TIME START  ************ "+startTime);
		ExceptionMessage exception = processorData.getException();
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		GraduationStudentRecord gradResponse = processorData.getGradResponse();
		if(gradResponse.getProgramCompletionDate() == null || gradResponse.getProgram().equalsIgnoreCase("SCCP")) {
			List<CodeDTO> optionalProgram = new ArrayList<>();
			GraduationData graduationDataStatus = gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), processorData.getAccessToken(),exception);
			
			if(graduationDataStatus != null && graduationDataStatus.getException() != null && graduationDataStatus.getException().getExceptionName() != null) {
				logger.info("**** Grad Algorithm Has Errors: ****");
				algorithmResponse.setException(graduationDataStatus.getException());
				processorData.setAlgorithmResponse(algorithmResponse);
				return processorData;
			}else if(exception.getExceptionName() != null) {
				logger.info("**** Grad Algorithm errored out: ****");
				algorithmResponse.setException(exception);
				processorData.setAlgorithmResponse(algorithmResponse);
				return processorData;
			}
			logger.info("**** Grad Algorithm Completed: ****");
			List<StudentOptionalProgram> projectedOptionalGradResponse = optionalProgramService.saveAndLogOptionalPrograms(graduationDataStatus,processorData.getStudentID(),processorData.getAccessToken(),optionalProgram);
			logger.info("**** Saved Optional Programs: ****");
			GraduationStudentRecord toBeSaved = gradStatusService.prepareGraduationStatusObj(graduationDataStatus);
			ReportData data = reportService.prepareReportData(graduationDataStatus,gradResponse,processorData.getAccessToken());
			logger.info("**** Prepared Data for Reports: ****");
			if(toBeSaved != null && toBeSaved.getStudentID() != null) {
				GraduationStudentRecord graduationStatusResponse = gradStatusService.saveStudentGradStatus(processorData.getStudentID(), processorData.getAccessToken(),toBeSaved,exception);
				logger.info("**** Saved Grad Status: ****");
				if(graduationDataStatus.isGraduated() && graduationStatusResponse.getProgramCompletionDate() != null) {			
					List<ProgramCertificate> certificateList =reportService.getCertificateList(gradResponse,graduationDataStatus,projectedOptionalGradResponse,processorData.getAccessToken(),exception);
					for(ProgramCertificate certType : certificateList) {
						reportService.saveStudentCertificateReportJasper(graduationStatusResponse,graduationDataStatus,processorData.getAccessToken(),certType);
					}
					logger.info("**** Saved Certificates: ****");
				}
				if(graduationDataStatus.getStudentCourses().getStudentCourseList().isEmpty() && graduationDataStatus.getStudentAssessments().getStudentAssessmentList().isEmpty()) {
					logger.info("**** No Transcript Generated: ****");
				}else {
					reportService.saveStudentTranscriptReportJasper(graduationStatusResponse.getPen(),data,processorData.getAccessToken(),graduationStatusResponse.getStudentID(),exception,graduationDataStatus.isGraduated());
					logger.info("**** Saved Reports: ****");
				}
				if(exception.getExceptionName() != null) {
					algorithmResponse.setException(exception);
					processorData.setAlgorithmResponse(algorithmResponse);
					gradStatusService.restoreStudentGradStatus(processorData.getStudentID(), processorData.getAccessToken(),graduationDataStatus.isGraduated());
					logger.info("**** Record Restored Due to Error: ****");
					return processorData;
				}
				algorithmResponse.setGraduationStudentRecord(graduationStatusResponse);
				algorithmResponse.setStudentOptionalProgram(projectedOptionalGradResponse);
			}
		}else {
			exception.setExceptionName("STUDENT-ALREADY-GRADUATED");
			exception.setExceptionDetails("Graduation Algorithm Cannot be Run for this graduated Student");
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
		processorData = inputData;
        logger.info("GraduateStudentProcess: ");
    }

}
