package ca.bc.gov.educ.api.graduation.service;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.CodeDTO;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.model.dto.ProcessorData;
import ca.bc.gov.educ.api.graduation.model.dto.ReportData;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcess;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcessFactory;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcessType;
import ca.bc.gov.educ.api.graduation.util.GradBusinessRuleException;
import ca.bc.gov.educ.api.graduation.util.GradValidation;


@Service
public class GraduationService {

	private static Logger logger = LoggerFactory.getLogger(GraduationService.class);

	@Autowired
    WebClient webClient;
	
	@Autowired
    RestTemplate restTemplate;
	
	@Autowired
	AlgorithmProcessFactory algorithmProcessFactory;
	
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
	
	public AlgorithmResponse graduateStudentByStudentID(String studentID, String accessToken) {
		logger.info("\n************* Graduating Student START  ************");
		long startTime = System.currentTimeMillis();
		logger.info("************* TIME START  ************ "+startTime);
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		List<CodeDTO> specialProgram = new ArrayList<>();
		try {
			logger.info("**** Getting Grad Stauts: ****" + studentID.substring(5));
			GraduationStatus gradResponse = gradStatusService.getGradStatus(studentID, accessToken);
			if(!gradResponse.getStudentStatus().equals("D") && !gradResponse.getStudentStatus().equals("M") && (gradResponse.getProgramCompletionDate() == null || gradResponse.getProgram().equalsIgnoreCase("SCCP"))) {
				GraduationData graduationDataStatus = gradAlgorithmService.runGradAlgorithm(gradResponse.getPen(), gradResponse.getProgram(), accessToken);		
				logger.info("**** Grad Algorithm Completed: ****");
				List<GradStudentSpecialProgram> projectedSpecialGradResponse = specialProgramService.saveAndLogSpecialPrograms(graduationDataStatus,studentID,accessToken,specialProgram);
				GraduationStatus toBeSaved = gradStatusService.prepareGraduationStatusObj(graduationDataStatus);
				ReportData data = reportService.prepareReportData(graduationDataStatus,accessToken,specialProgram);
				if(toBeSaved != null && toBeSaved.getPen() != null) {
					GraduationStatus graduationStatusResponse = gradStatusService.saveStudentGradStatus(studentID, accessToken,toBeSaved);
					logger.info("**** Saved Grad Status: ****");
					List<String> certificateList = new ArrayList<>();
					if(graduationDataStatus.isGraduated() && !graduationStatusResponse.getProgram().equalsIgnoreCase("SCCP")) {				
						certificateList = reportService.getCertificateList(certificateList,gradResponse,graduationDataStatus,projectedSpecialGradResponse);
						for(String certType : certificateList) {
							reportService.saveStudentCertificateReport(graduationStatusResponse.getPen(),data,accessToken,certType,graduationStatusResponse.getStudentID());
						}
					}
					logger.info("**** Saved Certificates: ****");
					data = reportService.setOtherRequiredData(data,graduationStatusResponse,graduationDataStatus,certificateList,accessToken);
					reportService.saveStudentAchievementReport(graduationStatusResponse.getPen(),data,accessToken,graduationStatusResponse.getStudentID());
					reportService.saveStudentTranscriptReport(graduationStatusResponse.getPen(),data,accessToken,graduationStatusResponse.getStudentID());			
					logger.info("**** Saved Reports: ****");
					algorithmResponse.setGraduationStatus(graduationStatusResponse);
					algorithmResponse.setSpecialGraduationStatus(projectedSpecialGradResponse);
					long endTime = System.currentTimeMillis();
					long diff = (endTime - startTime)/1000;
					logger.info("************* TIME Taken  ************ "+diff+" secs");
					return algorithmResponse;
				}
			}else {
				validation.addErrorAndStop("Graduation Algorithm Cannot be Run for this Student");
				return null;
			}
		}catch(Exception e) {
			throw new GradBusinessRuleException("Error Graduating Student. Please try again..." + e.getMessage());
		}
		return null;
	}

	public AlgorithmResponse projectStudentGraduationByStudentID(String studentID, String accessToken) {

		logger.info("\n************* PROJECTED : Graduating Student START  ************");
		long startTime = System.currentTimeMillis();
		logger.info("************* TIME START  ************ "+startTime);
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		try {
			logger.info("**** Getting Grad Stauts: ****" + studentID.substring(5));
			GraduationStatus gradResponse = gradStatusService.getGradStatus(studentID, accessToken);
			//Run Grad Algorithm
			if(gradResponse.getStudentStatus().equals("A") && (gradResponse.getProgramCompletionDate() == null || gradResponse.getProgram().equalsIgnoreCase("SCCP"))) {
				GraduationData graduationDataStatus = gradAlgorithmService.runProjectedAlgorithm(gradResponse.getPen(), gradResponse.getProgram(), accessToken);
				logger.info("**** Grad Algorithm Completed: ****");
				gradResponse = gradStatusService.processProjectedResults(gradResponse,graduationDataStatus);
				List<GradStudentSpecialProgram> projectedSpecialGradResponse = specialProgramService.projectedSpecialPrograms(graduationDataStatus, studentID, accessToken);
				algorithmResponse.setSpecialGraduationStatus(projectedSpecialGradResponse);
				algorithmResponse.setGraduationStatus(gradResponse);
				long endTime = System.currentTimeMillis();
				long diff = (endTime - startTime)/1000;
				logger.info("************* TIME Taken  ************ "+diff+" secs");
				return algorithmResponse;
			}else {
				validation.addErrorAndStop("Graduation Algorithm Cannot be Run for this Student");
				return null;
			}

		}catch(Exception e) {
			throw new GradBusinessRuleException("Error Projecting Student Graduation. Please try again..." + e.getMessage());
		}
	}
	
	public AlgorithmResponse graduateStudent(String studentID, String accessToken,String projectedType) {
		try {
			AlgorithmProcessType pType = AlgorithmProcessType.valueOf(StringUtils.toRootUpperCase(projectedType));
			GraduationStatus gradResponse = gradStatusService.getGradStatus(studentID, accessToken);
			if(!gradResponse.getStudentStatus().equals("D") && !gradResponse.getStudentStatus().equals("M")) {
				ProcessorData data = new ProcessorData(gradResponse,null,accessToken,studentID);
		     	AlgorithmProcess process = algorithmProcessFactory.createProcess(pType);
		     	if(process != null) {
			     	process.setInputData(data);
			     	data = process.fire();        
			        return data.getAlgorithmResponse();
		     	}else {
		     		validation.addErrorAndStop("Error Projecting Student Graduation. Please try again...");
		     	}
			}else {
				validation.addErrorAndStop("Graduation Algorithm Cannot be Run for this Student");
			}
			return null;
		}catch(Exception e) {
			throw new GradBusinessRuleException("Error Projecting Student Graduation. Please try again..." + e.getMessage());
		}
	}
}