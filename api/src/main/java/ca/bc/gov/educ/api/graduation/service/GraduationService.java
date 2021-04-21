package ca.bc.gov.educ.api.graduation.service;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.CodeDTO;
import ca.bc.gov.educ.api.graduation.model.dto.GenerateReport;
import ca.bc.gov.educ.api.graduation.model.dto.GradAlgorithmGraduationStatus;
import ca.bc.gov.educ.api.graduation.model.dto.GradCertificateTypes;
import ca.bc.gov.educ.api.graduation.model.dto.GradProgram;
import ca.bc.gov.educ.api.graduation.model.dto.GradSearchStudent;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudentCertificates;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudentReports;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationMessages;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.model.dto.ReportData;
import ca.bc.gov.educ.api.graduation.model.dto.SpecialGradAlgorithmGraduationStatus;
import ca.bc.gov.educ.api.graduation.model.dto.StudentDemographics;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiUtils;
import ca.bc.gov.educ.api.graduation.util.GradBusinessRuleException;
import reactor.core.publisher.Mono;


@Service
public class GraduationService {

	private static Logger logger = LoggerFactory.getLogger(GraduationService.class);

	@Autowired
    WebClient webClient;
	
	@Autowired
    RestTemplate restTemplate;

    @Value(EducGraduationApiConstants.ENDPOINT_GRADUATION_ALGORITHM_URL)
    private String graduateStudent;

	@Value(EducGraduationApiConstants.ENDPOINT_PROJECTED_GRADUATION_ALGORITHM_URL)
	private String projectedStudentGraduation;

    @Value(EducGraduationApiConstants.ENDPOINT_GRAD_STATUS_UPDATE_URL)
    private String updateGradStatusForStudent;

    @Value(EducGraduationApiConstants.ENDPOINT_ACHIEVEMENT_REPORT_API_URL)
    private String reportAchievementURL;
    
    @Value(EducGraduationApiConstants.ENDPOINT_CERTIFICATE_REPORT_API_URL)
    private String reportCertificateURL;

    @Value(EducGraduationApiConstants.ENDPOINT_TRANSCRIPT_REPORT_API_URL)
    private String reportTranscriptURL;

    @Value(EducGraduationApiConstants.ENDPOINT_GRAD_STUDENT_REPORT_UPDATE_URL)
    private String updateGradStudentReportForStudent;
    
    @Value(EducGraduationApiConstants.ENDPOINT_GRAD_STUDENT_CERTIFICATE_UPDATE_URL)
    private String updateGradStudentCertificateForStudent;

    @Value(EducGraduationApiConstants.ENDPOINT_GRAD_CERTIFICATE_TYPE_URL)
    private String getGradCertificateType;

    @Value(EducGraduationApiConstants.ENDPOINT_GRAD_PROGRAM_NAME_URL)
    private String getGradProgramName;
    
    @Value(EducGraduationApiConstants.ENDPOINT_SPECIAL_GRAD_STATUS_SAVE)
    private String saveSpecialGradStatusForStudent;
    
    @Value(EducGraduationApiConstants.ENDPOINT_SPECIAL_GRAD_STATUS_READ)
    private String readSpecialGradStatusForStudent;
    
    @Value(EducGraduationApiConstants.ENDPOINT_SPECIAL_PROGRAM_DETAILS_URL)
    private String specialProgramDetails;
    
	public AlgorithmResponse graduateStudentByPen(String pen, String accessToken) {
		logger.debug("graduateStudentByPen");
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		List<CodeDTO> specialProgram = new ArrayList<>();
		try {

		GraduationStatus gradResponse = webClient.get().uri(String.format(updateGradStatusForStudent,pen)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GraduationStatus.class).block();
		//Run Grad Algorithm
		GraduationData graduationDataStatus = webClient.get().uri(String.format(graduateStudent,pen,gradResponse.getProgram())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GraduationData.class).block();
		 
		List<GradStudentSpecialProgram> projectedSpecialGradResponse = new ArrayList<GradStudentSpecialProgram>();
		//Run Special Program Algorithm
		for(int i=0; i<graduationDataStatus.getSpecialGradStatus().size();i++) {
			CodeDTO specialProgramCode = new CodeDTO();
			SpecialGradAlgorithmGraduationStatus specialPrograms = graduationDataStatus.getSpecialGradStatus().get(i);
			GradStudentSpecialProgram gradSpecialProgram = webClient.get().uri(String.format(specialProgramDetails,pen,specialPrograms.getSpecialProgramID())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GradStudentSpecialProgram.class).block();
			gradSpecialProgram.setSpecialProgramCompletionDate(specialPrograms.getSpecialProgramCompletionDate());
			gradSpecialProgram.setStudentSpecialProgramData(new ObjectMapper().writeValueAsString(specialPrograms));
			
			//Save Special Grad Status
			webClient.post().uri(saveSpecialGradStatusForStudent).headers(h -> h.setBearerAuth(accessToken)).body(Mono.just(gradSpecialProgram), GradStudentSpecialProgram.class).retrieve().bodyToMono(GradStudentSpecialProgram.class);
			specialProgramCode.setCode(gradSpecialProgram.getSpecialProgramCode());
			specialProgramCode.setName(gradSpecialProgram.getSpecialProgramName());
			specialProgram.add(specialProgramCode);
			projectedSpecialGradResponse.add(gradSpecialProgram);
		}

		GraduationStatus toBeSaved = prepareGraduationStatusObj(graduationDataStatus);
		ReportData data = prepareReportData(graduationDataStatus,accessToken,specialProgram);
		if(toBeSaved != null && toBeSaved.getPen() != null) {
			GraduationStatus graduationStatusResponse = webClient.post().uri(String.format(updateGradStatusForStudent,pen)).headers(h -> h.setBearerAuth(accessToken)).body(Mono.just(toBeSaved), GraduationStatus.class).retrieve().bodyToMono(GraduationStatus.class).block();
			//Reports
			data.getDemographics().setMincode(graduationStatusResponse.getSchoolOfRecord());
			data.setIssueDate(EducGraduationApiUtils.formatDateForReport(graduationStatusResponse.getUpdatedTimestamp().toString()));
			data.setIsaDate(EducGraduationApiUtils.formatDateForReport(graduationStatusResponse.getUpdatedTimestamp().toString()));
			data.setStudentName(data.getDemographics().getLegalFirstName()+" "+data.getDemographics().getLegalMiddleNames()+" "+data.getDemographics().getLegalLastName());
			
			data.setStudentSchool(data.getSchool().getSchoolName());
			if(graduationDataStatus.getSpecialGradStatus().size() > 0) {
				data.getGraduationMessages().setHasSpecialProgram(true);
			}
			data.setStudentCertificateDate(EducGraduationApiUtils.formatDateForReport(graduationStatusResponse.getUpdatedTimestamp().toString()));
			List<String> certificateList = new ArrayList<String>();
			if(graduationDataStatus.isGraduated()) {				
				if(gradResponse.getProgram().equalsIgnoreCase("2018-EN")) {				
					if(!graduationDataStatus.getSchool().getIndependentDesignation().equalsIgnoreCase("2") && !graduationDataStatus.getSchool().getIndependentDesignation().equalsIgnoreCase("9") ) {
						certificateList.add("E");
					}else {
						certificateList.add("EI");
					}
					if(projectedSpecialGradResponse.size() > 0) {
						for(GradStudentSpecialProgram specialPrograms : projectedSpecialGradResponse) {
							if(specialPrograms.getSpecialProgramCode().equals("FI")) {
								certificateList.add("F");
							}
						}
					}
				}else {
					certificateList.add("S");
				}
				
				for(String certType : certificateList) {
					saveStudentCertificateReport(pen,data,accessToken,certType,graduationStatusResponse.getStudentID());
				}
				
				
			}
			List<CodeDTO> certificateProgram = new ArrayList<>();
			for(String certType : certificateList) {
				CodeDTO cDTO = new CodeDTO();
				GradCertificateTypes gradCertificateTypes = webClient.get().uri(String.format(getGradCertificateType,certType)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GradCertificateTypes.class).block();
	    		if(gradCertificateTypes != null) {
	    			cDTO.setCode(gradCertificateTypes.getCode());
	    			cDTO.setName(gradCertificateTypes.getDescription());
	    		}
	    		certificateProgram.add(cDTO);
			}
			data.getGraduationMessages().setCertificateProgram(certificateProgram);
			data.getGraduationMessages().setHasCareerProgram(certificateProgram.size() > 0 ? true:false);
			saveStudentAchievementReport(pen,data,accessToken,graduationStatusResponse.getStudentID());
			saveStudentTranscriptReport(pen,data,accessToken,graduationStatusResponse.getStudentID());			

			algorithmResponse.setGraduationStatus(graduationStatusResponse);
			algorithmResponse.setSpecialGraduationStatus(projectedSpecialGradResponse);
			return algorithmResponse;
		}
		}catch(Exception e) {
			throw new GradBusinessRuleException("Error Graduating Student. Please try again...");
		}
		return null;
	}

	public AlgorithmResponse projectStudentGraduationByPen(String pen, String accessToken) {

		logger.debug("projectStudentGraduationByPen");
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		try {

			GraduationStatus gradResponse = webClient.get().uri(String.format(updateGradStatusForStudent,pen)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GraduationStatus.class).block();

			//Run Grad Algorithm
			GraduationData graduationDataStatus = webClient.get().uri(String.format(projectedStudentGraduation, pen,gradResponse.getProgram(), true)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GraduationData.class).block();
			
			gradResponse.setStudentGradData(new ObjectMapper().writeValueAsString(graduationDataStatus));
			gradResponse.setPen(graduationDataStatus.getGradStatus().getPen());
			gradResponse.setProgram(graduationDataStatus.getGradStatus().getProgram());
			gradResponse.setProgramCompletionDate(graduationDataStatus.getGradStatus().getProgramCompletionDate());
			gradResponse.setGpa(graduationDataStatus.getGradStatus().getGpa());
			gradResponse.setHonoursStanding(graduationDataStatus.getGradStatus().getHonoursStanding());
			gradResponse.setRecalculateGradStatus(graduationDataStatus.getGradStatus().getRecalculateGradStatus());
			gradResponse.setSchoolOfRecord(graduationDataStatus.getGradStatus().getSchoolOfRecord());
			gradResponse.setStudentGrade(graduationDataStatus.getGradStatus().getStudentGrade());
			algorithmResponse.setGraduationStatus(gradResponse);
			List<GradStudentSpecialProgram> projectedSpecialGradResponse = new ArrayList<GradStudentSpecialProgram>();
			//Run Special Program Algorithm
			for(int i=0; i<graduationDataStatus.getSpecialGradStatus().size();i++) {
				GradStudentSpecialProgram specialProgramProjectedObj = new GradStudentSpecialProgram();
				SpecialGradAlgorithmGraduationStatus specialPrograms = graduationDataStatus.getSpecialGradStatus().get(i);
				GradStudentSpecialProgram gradSpecialProgram = webClient.get().uri(String.format(specialProgramDetails,pen,specialPrograms.getSpecialProgramID())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GradStudentSpecialProgram.class).block();
				specialProgramProjectedObj.setSpecialProgramCompletionDate(specialPrograms.getSpecialProgramCompletionDate());
				specialProgramProjectedObj.setStudentSpecialProgramData(new ObjectMapper().writeValueAsString(specialPrograms));
				specialProgramProjectedObj.setPen(pen);
				specialProgramProjectedObj.setMainProgramCode(gradSpecialProgram.getMainProgramCode());
				specialProgramProjectedObj.setSpecialProgramCode(gradSpecialProgram.getSpecialProgramCode());
				specialProgramProjectedObj.setSpecialProgramName(gradSpecialProgram.getSpecialProgramName());
				projectedSpecialGradResponse.add(specialProgramProjectedObj);
			}
			algorithmResponse.setSpecialGraduationStatus(projectedSpecialGradResponse);
			return algorithmResponse;

		}catch(Exception e) {
			throw new GradBusinessRuleException("Error Projecting Student Graduation. Please try again...");
		}
	}

	public void saveStudentAchievementReport(String pen, ReportData data, String accessToken, UUID studentID) {
		String encodedPdfReportAchievement = generateStudentAchievementReport(data,accessToken);
		GradStudentReports requestObj = new GradStudentReports();
		requestObj.setPen(pen);
		requestObj.setStudentID(studentID);
		requestObj.setReport(encodedPdfReportAchievement);
		requestObj.setGradReportTypeCode("ACHV");
		webClient.post().uri(String.format(updateGradStudentReportForStudent,pen)).headers(h -> h.setBearerAuth(accessToken)).body(Mono.just(requestObj), GradStudentReports.class).retrieve().bodyToMono(GradStudentReports.class).block();
	}
	
	public void saveStudentCertificateReport(String pen, ReportData data, String accessToken,String certificateType, UUID studentID) {
		String encodedPdfReportCertificate = generateStudentCertificateReport(data,accessToken);
		GradStudentCertificates requestObj = new GradStudentCertificates();
		requestObj.setPen(pen);
		requestObj.setStudentID(studentID);
		requestObj.setCertificate(encodedPdfReportCertificate);
		requestObj.setGradCertificateTypeCode(certificateType);
		webClient.post().uri(String.format(updateGradStudentCertificateForStudent,pen)).headers(h -> h.setBearerAuth(accessToken)).body(Mono.just(requestObj), GradStudentCertificates.class).retrieve().bodyToMono(GradStudentCertificates.class).block();
	}

	public void saveStudentTranscriptReport(String pen, ReportData data, String accessToken,UUID studentID) {
		String encodedPdfReportTranscript = generateStudentTranscriptReport(data,accessToken);
		GradStudentReports requestObj = new GradStudentReports();
		requestObj.setPen(pen);
		requestObj.setReport(encodedPdfReportTranscript);
		requestObj.setStudentID(studentID);
		requestObj.setGradReportTypeCode("TRAN");
		webClient.post().uri(String.format(updateGradStudentReportForStudent,pen)).headers(h -> h.setBearerAuth(accessToken)).body(Mono.just(requestObj), GradStudentReports.class).retrieve().bodyToMono(GradStudentReports.class).block();
	}

	private String generateStudentTranscriptReport(ReportData data, String accessToken) {
		GenerateReport reportParams = new GenerateReport();
		reportParams.setData(data);
		byte[] bytesSAR = webClient.post().uri(reportTranscriptURL).headers(h -> h.setBearerAuth(accessToken)).body(Mono.just(reportParams), GenerateReport.class).retrieve().bodyToMono(byte[].class).block();
		byte[] encoded = Base64.encodeBase64(bytesSAR);
	    return new String(encoded,StandardCharsets.US_ASCII);
	}



	private String generateStudentAchievementReport(ReportData data, String accessToken) {
		GenerateReport reportParams = new GenerateReport();
		reportParams.setData(data);
		byte[] bytesSAR = webClient.post().uri(reportAchievementURL).headers(h -> h.setBearerAuth(accessToken)).body(Mono.just(reportParams), GenerateReport.class).retrieve().bodyToMono(byte[].class).block();
		byte[] encoded = Base64.encodeBase64(bytesSAR);
	    return new String(encoded,StandardCharsets.US_ASCII);

	}

	private String generateStudentCertificateReport(ReportData data, String accessToken) {
		GenerateReport reportParams = new GenerateReport();
		reportParams.setData(data);
		byte[] bytesSAR = webClient.post().uri(reportCertificateURL).headers(h -> h.setBearerAuth(accessToken)).body(Mono.just(reportParams), GenerateReport.class).retrieve().bodyToMono(byte[].class).block();
		byte[] encoded = Base64.encodeBase64(bytesSAR);
	    return new String(encoded,StandardCharsets.US_ASCII);

	}



	private ReportData prepareReportData(GraduationData graduationDataStatus, String accessToken,List<CodeDTO> specialProgram) {
		GradSearchStudent gradStudent = graduationDataStatus.getGradStudent();
		GradAlgorithmGraduationStatus gradAlgorithm = graduationDataStatus.getGradStatus();
		ReportData data = new ReportData();
		StudentDemographics studentDemo = new  StudentDemographics();
		BeanUtils.copyProperties(gradStudent, studentDemo);
		data.setDemographics(studentDemo);
		data.setStudentCourse(graduationDataStatus.getStudentCourses().getStudentCourseList());
		data.setStudentAssessment(graduationDataStatus.getStudentAssessments().getStudentAssessmentList());
		if(graduationDataStatus.getStudentExams() != null)
			data.setStudentExam(graduationDataStatus.getStudentExams().getStudentExamList());
		
		data.setSchool(graduationDataStatus.getSchool());
		GraduationMessages graduationMessages = new GraduationMessages();
		if(gradAlgorithm.getProgram() != null) {
			GradProgram gradProgram = webClient.get().uri(String.format(getGradProgramName,gradAlgorithm.getProgram())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GradProgram.class).block();
			graduationMessages.setGradProgram(gradProgram.getProgramName());
			data.getDemographics().setProgram(gradProgram.getProgramCode());
		}
		graduationMessages.setHonours(gradAlgorithm.getHonoursStanding());
		graduationMessages.setGpa(gradAlgorithm.getGpa());
		graduationMessages.setNonGradReasons(graduationDataStatus.getNonGradReasons());
		graduationMessages.setSpecialProgram(specialProgram);
		data.setGraduationMessages(graduationMessages);

		//get Transcript Banner
		if(graduationDataStatus.getSchool() != null) {
			if(graduationDataStatus.getSchool().getIndependentDesignation().equalsIgnoreCase("1")) {
				data.setTranscriptBanner("B.C. INDEPENDENT SCHOOLS – GROUP 1");
			}else if(graduationDataStatus.getSchool().getIndependentDesignation().equalsIgnoreCase("2")) {
				data.setTranscriptBanner("B.C. INDEPENDENT SCHOOLS – GROUP 2");
			}else if(graduationDataStatus.getSchool().getIndependentDesignation().equalsIgnoreCase("4")) {
				data.setTranscriptBanner("B.C. INDEPENDENT SCHOOLS – GROUP 4");
			}
		}

		return data;
	}



	private GraduationStatus prepareGraduationStatusObj(GraduationData graduationDataStatus) {
		GraduationStatus obj = new GraduationStatus();
		BeanUtils.copyProperties(graduationDataStatus.getGradStatus(), obj);
		try {
			obj.setStudentGradData(new ObjectMapper().writeValueAsString(graduationDataStatus));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return obj;
	}
}
