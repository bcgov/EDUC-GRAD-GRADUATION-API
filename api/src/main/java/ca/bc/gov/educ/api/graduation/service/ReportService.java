package ca.bc.gov.educ.api.graduation.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
import ca.bc.gov.educ.api.graduation.model.dto.StudentDemographics;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiUtils;
import reactor.core.publisher.Mono;

@Service
public class ReportService {

	@Autowired
    WebClient webClient;
	
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
    
    @Value(EducGraduationApiConstants.ENDPOINT_GRAD_PROGRAM_NAME_URL)
    private String getGradProgramName;
    
    @Value(EducGraduationApiConstants.ENDPOINT_GRAD_CERTIFICATE_TYPE_URL)
    private String getGradCertificateType;

	
	public void saveStudentAchievementReport(String pen, ReportData data, String accessToken, UUID studentID) {
		String encodedPdfReportAchievement = generateStudentAchievementReport(data,accessToken);
		GradStudentReports requestObj = new GradStudentReports();
		requestObj.setPen(pen);
		requestObj.setStudentID(studentID);
		requestObj.setReport(encodedPdfReportAchievement);
		requestObj.setGradReportTypeCode("ACHV");
		webClient.post().uri(String.format(updateGradStudentReportForStudent,pen)).headers(h -> h.setBearerAuth(accessToken)).body(Mono.just(requestObj), GradStudentReports.class).retrieve().bodyToMono(GradStudentReports.class);
	}
	
	public void saveStudentCertificateReport(String pen, ReportData data, String accessToken,String certificateType, UUID studentID) {
		String encodedPdfReportCertificate = generateStudentCertificateReport(data,accessToken);
		GradStudentCertificates requestObj = new GradStudentCertificates();
		requestObj.setPen(pen);
		requestObj.setStudentID(studentID);
		requestObj.setCertificate(encodedPdfReportCertificate);
		requestObj.setGradCertificateTypeCode(certificateType);
		webClient.post().uri(String.format(updateGradStudentCertificateForStudent,pen)).headers(h -> h.setBearerAuth(accessToken)).body(Mono.just(requestObj), GradStudentCertificates.class).retrieve().bodyToMono(GradStudentCertificates.class);
	}

	public void saveStudentTranscriptReport(String pen, ReportData data, String accessToken,UUID studentID) {
		String encodedPdfReportTranscript = generateStudentTranscriptReport(data,accessToken);
		GradStudentReports requestObj = new GradStudentReports();
		requestObj.setPen(pen);
		requestObj.setReport(encodedPdfReportTranscript);
		requestObj.setStudentID(studentID);
		requestObj.setGradReportTypeCode("TRAN");
		webClient.post().uri(String.format(updateGradStudentReportForStudent,pen)).headers(h -> h.setBearerAuth(accessToken)).body(Mono.just(requestObj), GradStudentReports.class).retrieve().bodyToMono(GradStudentReports.class);
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



	public ReportData prepareReportData(GraduationData graduationDataStatus, String accessToken,List<CodeDTO> specialProgram) {
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

	public ReportData setOtherRequiredData(ReportData data, GraduationStatus graduationStatusResponse, GraduationData graduationDataStatus, List<String> certificateList,String accessToken) {
		data.getDemographics().setMincode(graduationStatusResponse.getSchoolOfRecord());
		data.setIssueDate(EducGraduationApiUtils.formatDateForReport(graduationStatusResponse.getUpdatedTimestamp().toString()));
		data.setIsaDate(EducGraduationApiUtils.formatDateForReport(graduationStatusResponse.getUpdatedTimestamp().toString()));
		data.setStudentName(data.getDemographics().getLegalFirstName()+" "+data.getDemographics().getLegalMiddleNames()+" "+data.getDemographics().getLegalLastName());
		
		data.setStudentSchool(data.getSchool().getSchoolName());
		if(graduationDataStatus.getSpecialGradStatus().size() > 0) {
			data.getGraduationMessages().setHasSpecialProgram(true);
		}
		data.setStudentCertificateDate(EducGraduationApiUtils.formatDateForReport(graduationStatusResponse.getUpdatedTimestamp().toString()));
		
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
		return data;
	}
	
	public List<String> getCertificateList(List<String> certificateList, GraduationStatus gradResponse, GraduationData graduationDataStatus, List<GradStudentSpecialProgram> projectedSpecialGradResponse) {
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
		return certificateList;
	}
}
