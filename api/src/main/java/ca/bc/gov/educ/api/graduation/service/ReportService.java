package ca.bc.gov.educ.api.graduation.service;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.graduation.model.dto.GradRequirement;
import ca.bc.gov.educ.api.graduation.model.dto.GradSearchStudent;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudentCertificates;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudentReports;
import ca.bc.gov.educ.api.graduation.model.dto.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.model.dto.StudentCourse;
import ca.bc.gov.educ.api.graduation.model.report.Address;
import ca.bc.gov.educ.api.graduation.model.report.Certificate;
import ca.bc.gov.educ.api.graduation.model.report.CertificateType;
import ca.bc.gov.educ.api.graduation.model.report.Code;
import ca.bc.gov.educ.api.graduation.model.report.Course;
import ca.bc.gov.educ.api.graduation.model.report.GradProgram;
import ca.bc.gov.educ.api.graduation.model.report.GraduationData;
import ca.bc.gov.educ.api.graduation.model.report.Mark;
import ca.bc.gov.educ.api.graduation.model.report.NonGradReason;
import ca.bc.gov.educ.api.graduation.model.report.OrderType;
import ca.bc.gov.educ.api.graduation.model.report.PaperType;
import ca.bc.gov.educ.api.graduation.model.report.Pen;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.model.report.ReportOptions;
import ca.bc.gov.educ.api.graduation.model.report.ReportRequest;
import ca.bc.gov.educ.api.graduation.model.report.School;
import ca.bc.gov.educ.api.graduation.model.report.Student;
import ca.bc.gov.educ.api.graduation.model.report.Transcript;
import ca.bc.gov.educ.api.graduation.model.report.TranscriptResult;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiUtils;

@Service
public class ReportService {

	@Autowired
    WebClient webClient;
	
	@Autowired
    EducGraduationApiConstants educGraduationApiConstants;

	public List<String> getCertificateList(List<String> certificateList, GraduationStatus gradResponse, ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, List<GradStudentSpecialProgram> projectedSpecialGradResponse) {
		if(gradResponse.getProgram().equalsIgnoreCase("2018-EN")) {				
			certificateList = checkSchoolForCertDecision(graduationDataStatus,certificateList);
			if(!projectedSpecialGradResponse.isEmpty()) {
				for(GradStudentSpecialProgram specialPrograms : projectedSpecialGradResponse) {
					if(specialPrograms.getSpecialProgramCode().equals("FI") && specialPrograms.getSpecialProgramCompletionDate() != null){
						certificateList.add("F");
					}
				}
			}
		}else {
			certificateList.add("S");
			if(graduationDataStatus.isDualDogwood()) {
				certificateList = checkSchoolForCertDecision(graduationDataStatus,certificateList);
			}
		}
		return certificateList;
	}
	
	public List<String> checkSchoolForCertDecision(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, List<String> certificateList) {
		if(!graduationDataStatus.getSchool().getIndependentDesignation().equalsIgnoreCase("2") && !graduationDataStatus.getSchool().getIndependentDesignation().equalsIgnoreCase("9") ) {
			certificateList.add("E");
		}else {
			certificateList.add("EI");
		}
		return certificateList;
	}

	public ReportData prepareReportData(
			ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, GraduationStatus gradResponse,String accessToken) {
		ReportData data = new ca.bc.gov.educ.api.graduation.model.report.ReportData();		
		data.setSchool(getSchoolData(graduationDataStatus.getSchool()));
		data.setStudent(getStudentData(graduationDataStatus.getGradStudent()));
		data.setGradMessage(graduationDataStatus.getGradMessage());
		data.setGradProgram(getGradProgram(graduationDataStatus,accessToken));
		data.setGraduationData(getGraduationData(graduationDataStatus));
		data.setLogo(StringUtils.startsWith(data.getSchool().getMincode(), "098") ? "YU":"BC");
		data.setTranscript(getTranscriptData(graduationDataStatus,gradResponse));
		data.setNonGradReasons(getNonGradReasons(graduationDataStatus.getNonGradReasons()));
		return data;
	}

	private List<NonGradReason> getNonGradReasons(List<GradRequirement> nonGradReasons) {
		List<NonGradReason> nList = new ArrayList<>();
		if(nonGradReasons != null) {
			for(GradRequirement gR:nonGradReasons) {
				NonGradReason obj = new NonGradReason();
				obj.setCode(gR.getRule());
				obj.setDescription(gR.getDescription());
				nList.add(obj);
			}
		}
		return nList;
	}

	private Transcript getTranscriptData(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, GraduationStatus gradResponse) {
		Transcript transcriptData = new Transcript();
		transcriptData.setInterim("false");
		transcriptData.setIssueDate(EducGraduationApiUtils.formatDateForReportJasper(gradResponse.getUpdatedTimestamp().toString()));
		transcriptData.setResults(getTranscriptResults(graduationDataStatus));
		return transcriptData;
	}

	private List<TranscriptResult> getTranscriptResults(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus) {
		List<TranscriptResult> tList = new ArrayList<>();
		List<StudentCourse> studentCourseList = graduationDataStatus.getStudentCourses().getStudentCourseList();
		for(StudentCourse sc:studentCourseList) {
			if(!sc.isDuplicate() && !sc.isFailed() && !sc.isNotCompleted() && !sc.isProjected()) {
				TranscriptResult result = new TranscriptResult();
				Course crse = new Course();
				crse.setCode(sc.getCourseCode());
				crse.setCredits(sc.getCredits().toString());
				crse.setLevel(sc.getCourseLevel());
				crse.setName(getCourseNameLogic(sc));
				crse.setRelatedCourse(sc.getRelatedCourse());
				crse.setRelatedLevel(sc.getRelatedLevel());
				crse.setType(sc.getEquivOrChallenge().equals("E") ? "1":"2");
				crse.setSessionDate(sc.getSessionDate().replace("/",""));
				result.setCourse(crse);
				
				Mark mrk = new Mark();
				
				mrk.setExamPercent(getValue(sc.getBestExamPercent()));
				mrk.setFinalLetterGrade(sc.getCompletedCourseLetterGrade());
				mrk.setFinalPercent(getValue(sc.getCompletedCoursePercentage()));
				mrk.setInterimLetterGrade(sc.getInterimLetterGrade());
				mrk.setInterimPercent(getValue(sc.getInterimPercent()));
				mrk.setSchoolPercent(getValue(sc.getBestSchoolPercent()));
				result.setMark(mrk);
				result.setRequirement(sc.getGradReqMet());
				result.setUsedForGrad(sc.getCreditsUsedForGrad() != null ? sc.getCreditsUsedForGrad().toString():"");
				result.setRequirementName(sc.getGradReqMetDetail());
				result.setEquivalency(sc.getEquivOrChallenge());
				tList.add(result);
			}
		}
		
		return tList;
	}
	
	private String getCourseNameLogic(StudentCourse sc) {
		if(sc.getGenericCourseType() != null && sc.getGenericCourseType().equalsIgnoreCase("I") && StringUtils.isNotBlank(sc.getRelatedCourse()) && StringUtils.isNotBlank(sc.getRelatedLevel()) && StringUtils.isNotBlank(sc.getRelatedCourseName())) {
			return "IDS "+sc.getRelatedCourseName();
		}
		if (StringUtils.equalsAnyIgnoreCase(sc.getCourseCode(), "FNA", "ASK", "FNASK") && StringUtils.isNotBlank(sc.getCourseDescription())) {
			return sc.getCourseDescription();
		}
		return sc.getCourseName();
	}

	private String getValue(Double value) {
		return value != null && value != 0.0 ? new DecimalFormat("#").format(value):"";
	}

	private GraduationData getGraduationData(
			ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus) {
		GraduationData data = new GraduationData();
		data.setDogwoodFlag(graduationDataStatus.isDualDogwood());
		if(graduationDataStatus.isGraduated()) {
			data.setGraduationDate(graduationDataStatus.getGradStatus().getProgramCompletionDate());
			data.setHonorsFlag(graduationDataStatus.getGradStatus().getHonoursStanding().equals("Y"));			
		}
		
		return data;
	}

	private GradProgram getGradProgram(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, String accessToken) {
		GradProgram gPgm = new GradProgram();
		Code code = new Code();
		if(graduationDataStatus.getGradStatus().getProgram() != null) {
			ca.bc.gov.educ.api.graduation.model.dto.GradProgram gradProgram = webClient.get().uri(String.format(educGraduationApiConstants.getProgramNameEndpoint(),graduationDataStatus.getGradStatus().getProgram())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(ca.bc.gov.educ.api.graduation.model.dto.GradProgram.class).block();
			code.setDescription(gradProgram.getProgramName());
			code.setName(gradProgram.getProgramName());
		}		
		code.setCode(graduationDataStatus.getGradStatus().getProgram());
		gPgm.setCode(code);
		return gPgm;
	}

	private Student getStudentData(GradSearchStudent gradStudent) {
		Student std = new Student();
		std.setBirthdate(EducGraduationApiUtils.parseDate(gradStudent.getDob()));
		std.setGrade(gradStudent.getStudentGrade());
		std.setStudStatus(gradStudent.getStudentStatus());
		std.setFirstName(gradStudent.getLegalFirstName());
		std.setGender(gradStudent.getGenderCode());
		std.setLastName(gradStudent.getLegalLastName());
		Pen pen = new Pen();
		pen.setPen(gradStudent.getPen());
		std.setPen(pen);
		return std;
	}

	private School getSchoolData(ca.bc.gov.educ.api.graduation.model.dto.School school) {
		School schObj = new School();
		Address addRess = new Address();
		addRess.setCity(school.getCity());
		addRess.setCode(school.getPostal());
		addRess.setCountry(school.getCountryCode());
		addRess.setRegion(school.getProvCode());
		addRess.setStreetLine1(school.getAddress1());
		addRess.setStreetLine2(school.getAddress2());
		schObj.setTypeIndicator(school.getIndependentDesignation());
		schObj.setAddress(addRess);
		schObj.setMincode(school.getMinCode());
		schObj.setName(school.getSchoolName());
		schObj.setSignatureCode(school.getSignatureDistrict());
		return schObj;
	}

	public void saveStudentTranscriptReportJasper(String pen,
			ca.bc.gov.educ.api.graduation.model.report.ReportData sample, String accessToken, UUID studentID) {
	
		String encodedPdfReportTranscript = generateStudentTranscriptReportJasper(sample,accessToken);
		GradStudentReports requestObj = new GradStudentReports();
		requestObj.setPen(pen);
		requestObj.setReport(encodedPdfReportTranscript);
		requestObj.setStudentID(studentID);
		requestObj.setGradReportTypeCode("TRAN");
		webClient.post().uri(String.format(educGraduationApiConstants.getUpdateGradStudentReport(),pen)).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(requestObj)).retrieve().bodyToMono(GradStudentReports.class).block();
		
	}

	private String generateStudentTranscriptReportJasper(ca.bc.gov.educ.api.graduation.model.report.ReportData sample,
			String accessToken) {
		ReportOptions options = new ReportOptions();
		options.setReportFile("transcript");
		options.setReportName("Transcript Report.pdf");
		ReportRequest reportParams = new ReportRequest();
		reportParams.setOptions(options);
		reportParams.setData(sample);		
		byte[] bytesSAR = webClient.post().uri(educGraduationApiConstants.getTranscriptReport()).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(reportParams)).retrieve().bodyToMono(byte[].class).block();
		byte[] encoded = Base64.encodeBase64(bytesSAR);
	    return new String(encoded,StandardCharsets.US_ASCII);
	}

	public ReportData prepareCertificateData(
			ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus,String accessToken) {
		ca.bc.gov.educ.api.graduation.model.report.ReportData data = new ca.bc.gov.educ.api.graduation.model.report.ReportData();
		data.setSchool(getSchoolData(graduationDataStatus.getSchool()));
		data.setStudent(getStudentData(graduationDataStatus.getGradStudent()));
		data.setGradProgram(getGradProgram(graduationDataStatus,accessToken));
		data.setGraduationData(getGraduationData(graduationDataStatus));
		return data;
	}

	public void saveStudentCertificateReportJasper(GraduationStatus gradResponse,ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, String accessToken,
			String certType) {
		ReportData certData = prepareCertificateData(graduationDataStatus,accessToken);
		certData.setUpdateDate(EducGraduationApiUtils.formatDateForReportJasper(gradResponse.getUpdatedTimestamp().toString()));
		certData.setCertificate(getCertificateData(gradResponse));
		if(certType.equalsIgnoreCase("E") || certType.equalsIgnoreCase("EI")) {
			certData.getStudent().setEnglishCert(certType);
		}else if(certType.equalsIgnoreCase("F") || certType.equalsIgnoreCase("S")) {
			certData.getStudent().setFrenchCert(certType);
		}
		String encodedPdfReportCertificate = generateStudentCertificateReportJasper(certData,accessToken);
		GradStudentCertificates requestObj = new GradStudentCertificates();
		requestObj.setPen(gradResponse.getPen());
		requestObj.setStudentID(gradResponse.getStudentID());
		requestObj.setCertificate(encodedPdfReportCertificate);
		requestObj.setGradCertificateTypeCode(certType);
		webClient.post().uri(educGraduationApiConstants.getUpdateGradStudentCertificate()).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(requestObj)).retrieve().bodyToMono(GradStudentCertificates.class).block();
		
	}
	
	private Certificate getCertificateData(GraduationStatus gradResponse) {
		Certificate cert = new Certificate();
		cert.setIssued(EducGraduationApiUtils.formatDateForReportJasper(gradResponse.getUpdatedTimestamp().toString()));
		OrderType orTy = new OrderType();
		orTy.setName("Certificate");
		CertificateType certType = new CertificateType();
		PaperType pType = new PaperType();
		String code = "Regular";
		if(gradResponse.getProgram().contains("SCCP")) {
			code="SCCP";
		}else if(gradResponse.getProgram().contains("1950")) {
			code="Adult";
		}
		pType.setCode(code);		
		certType.setPaperType(pType);
		certType.setReportName("Certificate");
		orTy.setCertificateType(certType);
		cert.setOrderType(orTy);
		return cert;
	}

	private String generateStudentCertificateReportJasper(ca.bc.gov.educ.api.graduation.model.report.ReportData sample,
			String accessToken) {
		ReportOptions options = new ReportOptions();
		options.setReportFile("certificate");
		options.setReportName("Certificate.pdf");
		ReportRequest reportParams = new ReportRequest();
		reportParams.setOptions(options);
		reportParams.setData(sample);		
		byte[] bytesSAR = webClient.post().uri(educGraduationApiConstants.getCertificateReport()).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(reportParams)).retrieve().bodyToMono(byte[].class).block();
		byte[] encoded = Base64.encodeBase64(bytesSAR);
	    return new String(encoded,StandardCharsets.US_ASCII);
	}
	
	private String generateStudentAchievementReportJasper(ReportData data, String accessToken) {
		ReportOptions options = new ReportOptions();
		options.setReportFile("achievement");
		options.setReportName("Student Achievement Report.pdf");
		ReportRequest reportParams = new ReportRequest();
		reportParams.setOptions(options);
		reportParams.setData(data);
		byte[] bytesSAR = webClient.post().uri(educGraduationApiConstants.getAchievementReport()).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(reportParams)).retrieve().bodyToMono(byte[].class).block();
		byte[] encoded = Base64.encodeBase64(bytesSAR);
	    return new String(encoded,StandardCharsets.US_ASCII);

	}
}
