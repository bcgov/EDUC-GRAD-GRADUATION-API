package ca.bc.gov.educ.api.graduation.service;


import java.util.ArrayList;
import java.util.List;

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
import ca.bc.gov.educ.api.graduation.model.dto.ReportData;
import ca.bc.gov.educ.api.graduation.util.GradBusinessRuleException;


@Service
public class GraduationService {

	private static Logger logger = LoggerFactory.getLogger(GraduationService.class);

	@Autowired
    WebClient webClient;
	
	@Autowired
    RestTemplate restTemplate;
	
	@Autowired
	GradStatusService gradStatusService;
	
	@Autowired
	GradAlgorithmService gradAlgorithmService;
	
	@Autowired
	SpecialProgramService specialProgramService;
	
	@Autowired
	ReportService reportService;
	
	public AlgorithmResponse graduateStudentByStudentID(String studentID, String accessToken) {
		logger.info("\n************* Graduating Student START  ************");
		long startTime = System.currentTimeMillis();
		logger.info("************* TIME START  ************ "+startTime);
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		List<CodeDTO> specialProgram = new ArrayList<>();
		try {
		logger.info("**** Getting Grad Stauts: ****" + studentID.substring(5));
		GraduationStatus gradResponse = gradStatusService.getGradStatus(studentID, accessToken);
		GraduationData graduationDataStatus = gradAlgorithmService.runGradAlgorithm(gradResponse.getPen(), gradResponse.getProgram(), accessToken);		
		logger.info("**** Grad Algorithm Completed: ****");
		List<GradStudentSpecialProgram> projectedSpecialGradResponse = specialProgramService.saveAndLogSpecialPrograms(graduationDataStatus,studentID,accessToken,specialProgram);
		GraduationStatus toBeSaved = gradStatusService.prepareGraduationStatusObj(graduationDataStatus);
		ReportData data = reportService.prepareReportData(graduationDataStatus,accessToken,specialProgram);
		if(toBeSaved != null && toBeSaved.getPen() != null) {
			GraduationStatus graduationStatusResponse = gradStatusService.saveStudentGradStatus(studentID, accessToken,toBeSaved);
			logger.info("**** Saved Grad Status: ****");
			List<String> certificateList = new ArrayList<String>();
			if(graduationDataStatus.isGraduated()) {				
				certificateList = reportService.getCertificateList(certificateList,gradResponse,graduationDataStatus,projectedSpecialGradResponse);
				for(String certType : certificateList) {
					reportService.saveStudentCertificateReport(graduationStatusResponse.getPen(),data,accessToken,certType,graduationStatusResponse.getStudentID());
				}
			}
			data = reportService.setOtherRequiredData(data,graduationStatusResponse,graduationDataStatus,certificateList,accessToken);
			reportService.saveStudentAchievementReport(graduationStatusResponse.getPen(),data,accessToken,graduationStatusResponse.getStudentID());
			reportService.saveStudentTranscriptReport(graduationStatusResponse.getPen(),data,accessToken,graduationStatusResponse.getStudentID());			

			algorithmResponse.setGraduationStatus(graduationStatusResponse);
			algorithmResponse.setSpecialGraduationStatus(projectedSpecialGradResponse);
			long endTime = System.currentTimeMillis();
			long diff = (endTime - startTime)/1000;
			logger.info("************* TIME Taken  ************ "+diff+" secs");
			return algorithmResponse;
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

		}catch(Exception e) {
			throw new GradBusinessRuleException("Error Projecting Student Graduation. Please try again..." + e.getMessage());
		}
	}	
}