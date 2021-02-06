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

import ca.bc.gov.educ.api.graduation.model.dto.CodeDTO;
import ca.bc.gov.educ.api.graduation.model.dto.GenerateReport;
import ca.bc.gov.educ.api.graduation.model.dto.GradAlgorithmGraduationStatus;
import ca.bc.gov.educ.api.graduation.model.dto.GradCertificateTypes;
import ca.bc.gov.educ.api.graduation.model.dto.GradProgram;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudent;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudentReport;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudentReports;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationMessages;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.model.dto.ReportData;
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
    
    @Value(EducGraduationApiConstants.ENDPOINT_GRAD_STATUS_UPDATE_URL)
    private String updateGradStatusForStudent;   
    
    @Value(EducGraduationApiConstants.ENDPOINT_ACHIEVEMENT_REPORT_API_URL)
    private String reportAchievementURL;
    
    @Value(EducGraduationApiConstants.ENDPOINT_TRANSCRIPT_REPORT_API_URL)
    private String reportTranscriptURL;
    
    @Value(EducGraduationApiConstants.ENDPOINT_GRAD_STUDENT_REPORT_UPDATE_URL)
    private String updateGradStudentReportForStudent;
    
    @Value(EducGraduationApiConstants.ENDPOINT_GRAD_CERTIFICATE_TYPE_URL)
    private String getGradCertificateType;
    
    @Value(EducGraduationApiConstants.ENDPOINT_GRAD_PROGRAM_NAME_URL)
    private String getGradProgramName;
    

    
	public GraduationStatus graduateStudentByPen(String pen, String accessToken) {
		logger.debug("graduateStudentByPen");
		HttpHeaders httpHeaders = EducGraduationApiUtils.getHeaders(accessToken);
		try {
			
		GraduationStatus gradResponse = restTemplate.exchange(String.format(updateGradStatusForStudent,pen), HttpMethod.GET,
					new HttpEntity<>(httpHeaders), GraduationStatus.class).getBody();
		logger.debug("Receieved PEN and Grad PRogram");
		logger.debug("Calling Grad Algorithm");
		GraduationData graduationDataStatus = restTemplate.exchange(String.format(graduateStudent,pen,gradResponse.getProgram()), HttpMethod.GET,
				new HttpEntity<>(httpHeaders), GraduationData.class).getBody();
		logger.debug("Algorithm complete");
		GraduationStatus toBeSaved = prepareGraduationStatusObj(graduationDataStatus);
		ReportData data = prepareReportData(graduationDataStatus,httpHeaders);
		if(toBeSaved != null && toBeSaved.getPen() != null) {
			logger.debug("Save Student Grad status");
			GraduationStatus graduationStatusResponse = restTemplate.exchange(String.format(updateGradStatusForStudent,pen), HttpMethod.POST,
					new HttpEntity<>(toBeSaved,httpHeaders), GraduationStatus.class).getBody();
			logger.debug("Save Student Grad status Complete");
			logger.debug("Report Create Call");
			data.getDemographics().setMinCode(graduationStatusResponse.getSchoolOfRecord());
			data.setIssueDate(EducGraduationApiUtils.formatDateForReport(graduationStatusResponse.getUpdatedTimestamp().toString()));
			data.setIsaDate(EducGraduationApiUtils.formatDateForReport(graduationStatusResponse.getUpdatedTimestamp().toString()));
			String encodedPdfReportAchievement = generateStudentAchievementReport(data,httpHeaders);
			String encodedPdfReportTranscript = generateStudentTranscriptReport(data,httpHeaders);
			GradStudentReports requestObj = new GradStudentReports();
			//Save Student Achievement Report
			requestObj.setPen(pen);
			requestObj.setReport(encodedPdfReportAchievement);
			requestObj.setGradReportTypeCode("ACHV");
			
			logger.debug("Report Save Call");
			restTemplate.exchange(String.format(updateGradStudentReportForStudent,pen), HttpMethod.POST,
							new HttpEntity<>(requestObj,httpHeaders), GradStudentReport.class).getBody();
			requestObj = new GradStudentReports();
			//Save Student Transcript Report
			requestObj.setPen(pen);
			requestObj.setReport(encodedPdfReportTranscript);
			requestObj.setGradReportTypeCode("TRAN");
			logger.debug("Report Save Call");
			restTemplate.exchange(String.format(updateGradStudentReportForStudent,pen), HttpMethod.POST,
							new HttpEntity<>(requestObj,httpHeaders), GradStudentReport.class).getBody();
			return graduationStatusResponse;
		}
		}catch(Exception e) {
			throw new GradBusinessRuleException("Error Graduating Student. Please try again...");
		}
		return null;
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



	private ReportData prepareReportData(GraduationData graduationDataStatus, HttpHeaders httpHeaders) {
		GradStudent gradStudent = graduationDataStatus.getGradStudent();	
		GradAlgorithmGraduationStatus gradAlgorithm = graduationDataStatus.getGradStatus();
		ReportData data = new ReportData();
		StudentDemographics studentDemo = new  StudentDemographics();
		BeanUtils.copyProperties(gradStudent, studentDemo);
		data.setDemographics(studentDemo);
		data.setStudentCourse(graduationDataStatus.getStudentCourses().getStudentCourseList());
		data.setStudentAssessment(graduationDataStatus.getStudentAssessments().getStudentAssessmentList());
		data.setStudentExam(graduationDataStatus.getStudentExams().getStudentExamList());
		data.setSchool(graduationDataStatus.getSchool());
		GraduationMessages graduationMessages = new GraduationMessages();
		if(gradAlgorithm.getProgram() != null) {
			GradProgram gradProgram = restTemplate.exchange(String.format(getGradProgramName,gradAlgorithm.getProgram()), HttpMethod.GET,
    				new HttpEntity<>(httpHeaders), GradProgram.class).getBody();
			graduationMessages.setGradProgram(gradProgram.getProgramName());
		}
		graduationMessages.setHonours(gradAlgorithm.getHonoursFlag());
		graduationMessages.setGpa(gradAlgorithm.getGpa());
		graduationMessages.setNonGradReasons(graduationDataStatus.getNonGradReasons());
		List<CodeDTO> specialProgram = new ArrayList<>();
		List<CodeDTO> certificateProgram = new ArrayList<>();
		
		CodeDTO cDTO = null;
		graduationMessages.setSpecialProgram(specialProgram);
		if(gradAlgorithm.getCertificateType1() != null) {
			cDTO = new CodeDTO();
			GradCertificateTypes gradCertificateTypes = restTemplate.exchange(String.format(getGradCertificateType,gradAlgorithm.getCertificateType1()), HttpMethod.GET,
    				new HttpEntity<>(httpHeaders), GradCertificateTypes.class).getBody();
    		if(gradCertificateTypes != null) {
    			cDTO.setCode(gradCertificateTypes.getCode());
    			cDTO.setName(gradCertificateTypes.getDescription());
    		}			
    		certificateProgram.add(cDTO);
		}
		
		if(gradAlgorithm.getCertificateType2() != null) {
			cDTO = new CodeDTO();
			GradCertificateTypes gradCertificateTypes = restTemplate.exchange(String.format(getGradCertificateType,gradAlgorithm.getCertificateType2()), HttpMethod.GET,
    				new HttpEntity<>(httpHeaders), GradCertificateTypes.class).getBody();
    		if(gradCertificateTypes != null) {
    			cDTO.setCode(gradCertificateTypes.getCode());
    			cDTO.setName(gradCertificateTypes.getDescription());
    		}			
    		certificateProgram.add(cDTO);
		}
		graduationMessages.setCertificateProgram(certificateProgram);
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
