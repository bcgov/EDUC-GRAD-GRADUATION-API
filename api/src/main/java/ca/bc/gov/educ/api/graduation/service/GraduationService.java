package ca.bc.gov.educ.api.graduation.service;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.CodeDTO;
import ca.bc.gov.educ.api.graduation.model.dto.GenerateReport;
import ca.bc.gov.educ.api.graduation.model.dto.GradAlgorithmGraduationStatus;
import ca.bc.gov.educ.api.graduation.model.dto.GradCertificateTypes;
import ca.bc.gov.educ.api.graduation.model.dto.GradProgram;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudent;
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


@Service
public class GraduationService {

	private static Logger logger = LoggerFactory.getLogger(GraduationService.class);

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
    
    @Value(EducGraduationApiConstants.ENDPOINT_SPECIAL_GRADUATION_ALGORITHM_URL)
    private String specialProgramGraduateStudent;
    
    @Value(EducGraduationApiConstants.ENDPOINT_SPECIAL_PROGRAM_DETAILS_URL)
    private String specialProgramDetails;
    
    



	public AlgorithmResponse graduateStudentByPen(String pen, String accessToken) {
		logger.debug("graduateStudentByPen");
		AlgorithmResponse algorithmResponse = new AlgorithmResponse();
		HttpHeaders httpHeaders = EducGraduationApiUtils.getHeaders(accessToken);
		List<CodeDTO> specialProgram = new ArrayList<>();
		try {

		GraduationStatus gradResponse = restTemplate.exchange(String.format(updateGradStatusForStudent,pen), HttpMethod.GET,
					new HttpEntity<>(httpHeaders), GraduationStatus.class).getBody();
		//Run Grad Algorithm
		GraduationData graduationDataStatus = restTemplate.exchange(String.format(graduateStudent,pen,gradResponse.getProgram()), HttpMethod.GET,
				new HttpEntity<>(httpHeaders), GraduationData.class).getBody();
		List<GradStudentSpecialProgram> projectedSpecialGradResponse = new ArrayList<GradStudentSpecialProgram>();
		//Run Special Program Algorithm
		for(int i=0; i<graduationDataStatus.getSpecialGradStatus().size();i++) {
			CodeDTO specialProgramCode = new CodeDTO();
			SpecialGradAlgorithmGraduationStatus specialPrograms = graduationDataStatus.getSpecialGradStatus().get(i);
			GradStudentSpecialProgram gradSpecialProgram = restTemplate.exchange(String.format(specialProgramDetails,pen,specialPrograms.getSpecialProgramID()), HttpMethod.GET,
					new HttpEntity<>(httpHeaders), GradStudentSpecialProgram.class).getBody();
			
			gradSpecialProgram.setSpecialProgramCompletionDate(specialPrograms.getSpecialProgramCompletionDate());
			gradSpecialProgram.setStudentSpecialProgramData(new ObjectMapper().writeValueAsString(specialPrograms));
			
			restTemplate.exchange(saveSpecialGradStatusForStudent, HttpMethod.POST,
						new HttpEntity<>(gradSpecialProgram,httpHeaders), GradStudentSpecialProgram.class).getBody();
			
			specialProgramCode.setCode(gradSpecialProgram.getSpecialProgramCode());
			specialProgramCode.setName(gradSpecialProgram.getSpecialProgramName());
			specialProgram.add(specialProgramCode);
			projectedSpecialGradResponse.add(gradSpecialProgram);
		}

		GraduationStatus toBeSaved = prepareGraduationStatusObj(graduationDataStatus);
		ReportData data = prepareReportData(graduationDataStatus,httpHeaders,specialProgram);
		if(toBeSaved != null && toBeSaved.getPen() != null) {
			GraduationStatus graduationStatusResponse = restTemplate.exchange(String.format(updateGradStatusForStudent,pen), HttpMethod.POST,
					new HttpEntity<>(toBeSaved,httpHeaders), GraduationStatus.class).getBody();

			//Reports
			data.getDemographics().setMinCode(graduationStatusResponse.getSchoolOfRecord());
			data.setIssueDate(EducGraduationApiUtils.formatDateForReport(graduationStatusResponse.getUpdatedTimestamp().toString()));
			data.setIsaDate(EducGraduationApiUtils.formatDateForReport(graduationStatusResponse.getUpdatedTimestamp().toString()));
			data.setStudentName(data.getDemographics().getStudGiven()+" "+data.getDemographics().getStudMiddle()+" "+data.getDemographics().getStudSurname());
			data.setStudentSchool(data.getSchool().getSchoolName());
			if(graduationDataStatus.getSpecialGradStatus().size() > 0) {
				data.getGraduationMessages().setHasSpecialProgram(true);
			}
			data.setStudentCertificateDate(EducGraduationApiUtils.formatDateForReport(graduationStatusResponse.getUpdatedTimestamp().toString()));
			String certificateType="";
			if(graduationDataStatus.isGraduated()) {				
				if(gradResponse.getProgram().equalsIgnoreCase("2018-EN")) {				
					if(!graduationDataStatus.getSchool().getIndependentDesignation().equalsIgnoreCase("2") && !graduationDataStatus.getSchool().getIndependentDesignation().equalsIgnoreCase("9") ) {
						certificateType = "E";
					}else {
						certificateType = "EI";
					}
				}else {
					certificateType="S";
				}
				saveStudentCertificateReport(pen,data,httpHeaders,certificateType);
			}
			List<CodeDTO> certificateProgram = new ArrayList<>();
			CodeDTO cDTO = new CodeDTO();
			GradCertificateTypes gradCertificateTypes = restTemplate.exchange(String.format(getGradCertificateType,certificateType), HttpMethod.GET,
    				new HttpEntity<>(httpHeaders), GradCertificateTypes.class).getBody();
    		if(gradCertificateTypes != null) {
    			cDTO.setCode(gradCertificateTypes.getCode());
    			cDTO.setName(gradCertificateTypes.getDescription());
    		}
    		certificateProgram.add(cDTO);
			data.getGraduationMessages().setCertificateProgram(certificateProgram);
			saveStudentAchievementReport(pen,data,httpHeaders);
			saveStudentTranscriptReport(pen,data,httpHeaders);			

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
		HttpHeaders httpHeaders = EducGraduationApiUtils.getHeaders(accessToken);

		try {

			GraduationStatus gradResponse = restTemplate.exchange(String.format(updateGradStatusForStudent,pen), HttpMethod.GET,
					new HttpEntity<>(httpHeaders), GraduationStatus.class).getBody();

			//Run Grad Algorithm
			GraduationData graduationDataStatus = restTemplate.exchange(String.format(projectedStudentGraduation, pen,
					gradResponse.getProgram(), true), HttpMethod.GET,
					new HttpEntity<>(httpHeaders), GraduationData.class).getBody();

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
				GradStudentSpecialProgram gradSpecialProgram = restTemplate.exchange(String.format(specialProgramDetails,pen,specialPrograms.getSpecialProgramID()), HttpMethod.GET,
						new HttpEntity<>(httpHeaders), GradStudentSpecialProgram.class).getBody();
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

	public void saveStudentAchievementReport(String pen, ReportData data, HttpHeaders httpHeaders) {
		String encodedPdfReportAchievement = generateStudentAchievementReport(data,httpHeaders);
		GradStudentReports requestObj = new GradStudentReports();
		requestObj.setPen(pen);
		requestObj.setReport(encodedPdfReportAchievement);
		requestObj.setGradReportTypeCode("ACHV");
		restTemplate.exchange(String.format(updateGradStudentReportForStudent,pen), HttpMethod.POST,
						new HttpEntity<>(requestObj,httpHeaders), GradStudentReports.class).getBody();
	}
	
	public void saveStudentCertificateReport(String pen, ReportData data, HttpHeaders httpHeaders,String certificateType) {
		String encodedPdfReportCertificate = generateStudentCertificateReport(data,httpHeaders);
		GradStudentCertificates requestObj = new GradStudentCertificates();
		requestObj.setPen(pen);
		requestObj.setCertificate(encodedPdfReportCertificate);
		requestObj.setGradCertificateTypeCode(certificateType);
		restTemplate.exchange(String.format(updateGradStudentCertificateForStudent,pen), HttpMethod.POST,
						new HttpEntity<>(requestObj,httpHeaders), GradStudentCertificates.class).getBody();
	}

	public void saveStudentTranscriptReport(String pen, ReportData data, HttpHeaders httpHeaders) {
		String encodedPdfReportTranscript = generateStudentTranscriptReport(data,httpHeaders);
		GradStudentReports requestObj = new GradStudentReports();
		requestObj.setPen(pen);
		requestObj.setReport(encodedPdfReportTranscript);
		requestObj.setGradReportTypeCode("TRAN");
		restTemplate.exchange(String.format(updateGradStudentReportForStudent,pen), HttpMethod.POST,
						new HttpEntity<>(requestObj,httpHeaders), GradStudentReports.class).getBody();
	}

	private String generateStudentTranscriptReport(ReportData data, HttpHeaders httpHeaders) {
		GenerateReport reportParams = new GenerateReport();
		reportParams.setData(data);
		byte[] bytesSAR = restTemplate.exchange(reportTranscriptURL, HttpMethod.POST,
				new HttpEntity<>(reportParams,httpHeaders), byte[].class).getBody();
		byte[] encoded = Base64.encodeBase64(bytesSAR);
	    return new String(encoded,StandardCharsets.US_ASCII);
	}



	private String generateStudentAchievementReport(ReportData data, HttpHeaders httpHeaders) {
		GenerateReport reportParams = new GenerateReport();
		reportParams.setData(data);
		byte[] bytesSAR = restTemplate.exchange(reportAchievementURL, HttpMethod.POST,
				new HttpEntity<>(reportParams,httpHeaders), byte[].class).getBody();
		byte[] encoded = Base64.encodeBase64(bytesSAR);
	    return new String(encoded,StandardCharsets.US_ASCII);

	}

	private String generateStudentCertificateReport(ReportData data, HttpHeaders httpHeaders) {
		GenerateReport reportParams = new GenerateReport();
		reportParams.setData(data);
		byte[] bytesSAR = restTemplate.exchange(reportCertificateURL, HttpMethod.POST,
				new HttpEntity<>(reportParams,httpHeaders), byte[].class).getBody();
		byte[] encoded = Base64.encodeBase64(bytesSAR);
	    return new String(encoded,StandardCharsets.US_ASCII);

	}



	private ReportData prepareReportData(GraduationData graduationDataStatus, HttpHeaders httpHeaders,List<CodeDTO> specialProgram) {
		GradStudent gradStudent = graduationDataStatus.getGradStudent();
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
			GradProgram gradProgram = restTemplate.exchange(String.format(getGradProgramName,gradAlgorithm.getProgram()), HttpMethod.GET,
    				new HttpEntity<>(httpHeaders), GradProgram.class).getBody();
			graduationMessages.setGradProgram(gradProgram.getProgramName());
			data.getDemographics().setGradProgram(gradProgram.getProgramCode());
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
