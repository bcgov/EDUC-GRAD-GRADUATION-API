package ca.bc.gov.educ.api.graduation.process;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.CodeDTO;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.graduation.model.dto.ProcessorData;
import ca.bc.gov.educ.api.graduation.model.dto.ProgramCertificate;
import ca.bc.gov.educ.api.graduation.model.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.service.GradAlgorithmService;
import ca.bc.gov.educ.api.graduation.service.GradStatusService;
import ca.bc.gov.educ.api.graduation.service.ReportService;
import ca.bc.gov.educ.api.graduation.service.SpecialProgramService;
import ca.bc.gov.educ.api.graduation.util.GradBusinessRuleException;
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
	SpecialProgramService specialProgramService;

	@Autowired
	ReportService reportService;
	
	@Autowired
	GradValidation validation;
	
	@Override
	public ProcessorData fire() {
		
		try {
			long startTime = System.currentTimeMillis();
			logger.info("************* TIME START  ************ "+startTime);
			AlgorithmResponse algorithmResponse = new AlgorithmResponse();
			GraduationStudentRecord gradResponse = processorData.getGradResponse();
			if(gradResponse.getProgramCompletionDate() != null) {
				List<CodeDTO> specialProgram = new ArrayList<>();
				GraduationData graduationDataStatus = gradAlgorithmService.runGradAlgorithm(gradResponse.getStudentID(), gradResponse.getProgram(), processorData.getAccessToken());
				logger.info("**** Grad Algorithm Completed: ****");
				List<StudentOptionalProgram> projectedSpecialGradResponse = specialProgramService.saveAndLogSpecialPrograms(graduationDataStatus,processorData.getStudentID(),processorData.getAccessToken(),specialProgram);
				logger.info("**** Saved Optional Programs: ****");
				GraduationStudentRecord toBeSaved = gradStatusService.prepareGraduationStatusObj(graduationDataStatus);
				ReportData data = reportService.prepareReportData(graduationDataStatus,gradResponse,processorData.getAccessToken());
				logger.info("**** Prepared Data for Reports: ****");
				if(toBeSaved != null && toBeSaved.getStudentID() != null) {
					GraduationStudentRecord graduationStatusResponse = gradStatusService.saveStudentGradStatus(processorData.getStudentID(), processorData.getAccessToken(),toBeSaved);
					logger.info("**** Saved Grad Status: ****");
					if(graduationDataStatus.isGraduated() && graduationStatusResponse.getProgramCompletionDate() != null) {				
						List<ProgramCertificate> certificateList =  reportService.getCertificateList(gradResponse,graduationDataStatus,projectedSpecialGradResponse,processorData.getAccessToken());
						for(ProgramCertificate certType : certificateList) {
							reportService.saveStudentCertificateReportJasper(graduationStatusResponse,graduationDataStatus,processorData.getAccessToken(),certType);
						}
						logger.info("**** Saved Certificates: ****");
					}
					
					if(graduationDataStatus.getStudentCourses().getStudentCourseList().isEmpty() && graduationDataStatus.getStudentAssessments().getStudentAssessmentList().isEmpty()) {
						logger.info("**** No Transcript Generated: ****");
					}else {
						reportService.saveStudentTranscriptReportJasper(graduationStatusResponse.getPen(),data,processorData.getAccessToken(),graduationStatusResponse.getStudentID());
						logger.info("**** Saved Reports: ****");
					}
					algorithmResponse.setGraduationStudentRecord(graduationStatusResponse);
					algorithmResponse.setStudentOptionalProgram(projectedSpecialGradResponse);
				}
			}else {
				throw new GradBusinessRuleException("Graduation Algorithm Cannot be Run for this graduated Student");
			}
			long endTime = System.currentTimeMillis();
			long diff = (endTime - startTime)/1000;
			logger.info("************* TIME Taken  ************ "+diff+" secs");
			processorData.setAlgorithmResponse(algorithmResponse);
			return processorData;

		}catch(Exception e) {
			throw new GradBusinessRuleException(e.getMessage());
		}
	}

	@Override
    public void setInputData(ProcessorData inputData) {
		processorData = inputData;
        logger.info("ProjectedGradFinalMarksReportsProcess: ");
    }

}
