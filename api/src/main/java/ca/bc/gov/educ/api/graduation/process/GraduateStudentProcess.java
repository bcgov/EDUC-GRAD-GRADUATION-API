package ca.bc.gov.educ.api.graduation.process;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.CodeDTO;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.model.dto.ProcessorData;
import ca.bc.gov.educ.api.graduation.model.dto.ReportData;
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
public class GraduateStudentProcess implements AlgorithmProcess {
	
	private static Logger logger = LoggerFactory.getLogger(GraduateStudentProcess.class);
	
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
			GraduationStatus gradResponse = processorData.getGradResponse();
			if(gradResponse.getProgramCompletionDate() == null || gradResponse.getProgram().equalsIgnoreCase("SCCP")) {
				List<CodeDTO> specialProgram = new ArrayList<>();
				GraduationData graduationDataStatus = gradAlgorithmService.runGradAlgorithm(gradResponse.getPen(), gradResponse.getProgram(), processorData.getAccessToken());
				logger.info("**** Grad Algorithm Completed: ****");
				List<GradStudentSpecialProgram> projectedSpecialGradResponse = specialProgramService.saveAndLogSpecialPrograms(graduationDataStatus,processorData.getStudentID(),processorData.getAccessToken(),specialProgram);
				GraduationStatus toBeSaved = gradStatusService.prepareGraduationStatusObj(graduationDataStatus);
				ReportData data = reportService.prepareReportData(graduationDataStatus,processorData.getAccessToken(),specialProgram);
				if(toBeSaved != null && toBeSaved.getPen() != null) {
					GraduationStatus graduationStatusResponse = gradStatusService.saveStudentGradStatus(processorData.getStudentID(), processorData.getAccessToken(),toBeSaved);
					logger.info("**** Saved Grad Status: ****");
					List<String> certificateList = new ArrayList<>();
					if(graduationDataStatus.isGraduated() && !graduationStatusResponse.getProgram().equalsIgnoreCase("SCCP")) {				
						certificateList = reportService.getCertificateList(certificateList,gradResponse,graduationDataStatus,projectedSpecialGradResponse);
						for(String certType : certificateList) {
							reportService.saveStudentCertificateReport(graduationStatusResponse.getPen(),data,processorData.getAccessToken(),certType,graduationStatusResponse.getStudentID());
						}
					}
					logger.info("**** Saved Certificates: ****");
					data = reportService.setOtherRequiredData(data,graduationStatusResponse,graduationDataStatus,certificateList,processorData.getAccessToken());
					reportService.saveStudentAchievementReport(graduationStatusResponse.getPen(),data,processorData.getAccessToken(),graduationStatusResponse.getStudentID());
					reportService.saveStudentTranscriptReport(graduationStatusResponse.getPen(),data,processorData.getAccessToken(),graduationStatusResponse.getStudentID());			
					logger.info("**** Saved Reports: ****");
					algorithmResponse.setGraduationStatus(graduationStatusResponse);
					algorithmResponse.setSpecialGraduationStatus(projectedSpecialGradResponse);
				}
			}else {
				validation.addErrorAndStop("Graduation Algorithm Cannot be Run for this Student");
				return null;
			}
			long endTime = System.currentTimeMillis();
			long diff = (endTime - startTime)/1000;
			logger.info("************* TIME Taken  ************ "+diff+" secs");
			processorData.setAlgorithmResponse(algorithmResponse);
			return processorData;

		}catch(Exception e) {
			throw new GradBusinessRuleException("Error Projecting Student Graduation. Please try again..." + e.getMessage());
		}
	}

	@Override
    public void setInputData(ProcessorData inputData) {
		processorData = (ProcessorData)inputData;
        logger.info("GraduateStudentProcess: ");
    }

}
