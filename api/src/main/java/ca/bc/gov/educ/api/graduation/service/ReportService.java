package ca.bc.gov.educ.api.graduation.service;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import ca.bc.gov.educ.api.graduation.model.achvreport.*;
import ca.bc.gov.educ.api.graduation.model.achvreport.StudExam;
import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.dto.GradRequirement;
import ca.bc.gov.educ.api.graduation.model.dto.StudentAssessment;
import ca.bc.gov.educ.api.graduation.model.dto.StudentCourse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.graduation.model.dto.ProgramCertificateTranscript;
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
import ca.bc.gov.educ.api.graduation.model.report.Student;
import ca.bc.gov.educ.api.graduation.model.report.Transcript;
import ca.bc.gov.educ.api.graduation.model.report.TranscriptResult;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiUtils;

@Service
public class ReportService {

	private static final String GRAD_REPORT_API_DOWN = "GRAD-REPORT-API IS DOWN";
	private static final String GRAD_GRADUATION_REPORT_API_DOWN = "GRAD-GRADUATION-REPORT-API IS DOWN";
	private static final String DOCUMENT_STATUS_COMPLETED = "COMPL";
	@Autowired
	WebClient webClient;

	@Autowired
	EducGraduationApiConstants educGraduationApiConstants;

	public List<ProgramCertificateTranscript> getCertificateList(GraduationStudentRecord gradResponse, ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, List<StudentOptionalProgram> projectedOptionalGradResponse, String accessToken, ExceptionMessage exception) {
		ProgramCertificateReq req = new ProgramCertificateReq();
		req.setProgramCode(gradResponse.getProgram());
		for (StudentOptionalProgram optionalPrograms : projectedOptionalGradResponse) {
			if (optionalPrograms.isGraduated() && (optionalPrograms.getOptionalProgramCode().equals("FI") || optionalPrograms.getOptionalProgramCode().equals("DD"))) {
				req.setOptionalProgram(optionalPrograms.getOptionalProgramCode());
			}
		}
		req.setSchoolCategoryCode(getSchoolCategoryCode(accessToken, graduationDataStatus.getGradStatus().getSchoolOfRecord()));
		try {
			return webClient.post().uri(educGraduationApiConstants.getCertList()).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(req)).retrieve().bodyToMono(new ParameterizedTypeReference<List<ProgramCertificateTranscript>>() {
			}).block();
		} catch (Exception e) {
			exception.setExceptionName(GRAD_GRADUATION_REPORT_API_DOWN);
			exception.setExceptionDetails(e.getLocalizedMessage());
			return new ArrayList<>();
		}
	}

	public String getSchoolCategoryCode(String accessToken, String mincode) {
		CommonSchool commonSchoolObj = webClient.get().uri(String.format(educGraduationApiConstants.getSchoolCategoryCode(), mincode)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(CommonSchool.class).block();
		if (commonSchoolObj != null) {
			return commonSchoolObj.getSchoolCategoryCode();
		}
		return null;
	}

	public ReportData prepareReportData(
			ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, GraduationStudentRecord gradResponse, String accessToken) {
		ReportData data = new ca.bc.gov.educ.api.graduation.model.report.ReportData();
		data.setSchool(getSchoolData(graduationDataStatus.getSchool()));
		data.setStudent(getStudentData(graduationDataStatus.getGradStudent()));
		data.setGradMessage(graduationDataStatus.getGradMessage());
		data.setGradProgram(getGradProgram(graduationDataStatus, accessToken));
		data.setGraduationData(getGraduationData(graduationDataStatus));
		data.setLogo(StringUtils.startsWith(data.getSchool().getMincode(), "098") ? "YU" : "BC");
		data.setTranscript(getTranscriptData(graduationDataStatus, gradResponse, accessToken));
		data.setNonGradReasons(getNonGradReasons(graduationDataStatus.getNonGradReasons()));
		return data;
	}

	private List<NonGradReason> getNonGradReasons(List<GradRequirement> nonGradReasons) {
		List<NonGradReason> nList = new ArrayList<>();
		if (nonGradReasons != null) {
			for (GradRequirement gR : nonGradReasons) {
				NonGradReason obj = new NonGradReason();
				obj.setCode(gR.getRule());
				obj.setDescription(gR.getDescription());
				nList.add(obj);
			}
		}
		return nList;
	}

	private Transcript getTranscriptData(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, GraduationStudentRecord gradResponse, String accessToken) {
		Transcript transcriptData = new Transcript();
		transcriptData.setInterim("false");
		transcriptData.setIssueDate(EducGraduationApiUtils.formatDateForReportJasper(gradResponse.getUpdateDate().toString()));
		transcriptData.setResults(getTranscriptResults(graduationDataStatus, accessToken));
		return transcriptData;
	}

	private List<TranscriptResult> getTranscriptResults(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, String accessToken) {
		List<TranscriptResult> tList = new ArrayList<>();
		List<StudentCourse> studentCourseList = graduationDataStatus.getStudentCourses().getStudentCourseList();
		List<StudentAssessment> studentAssessmentList = graduationDataStatus.getStudentAssessments().getStudentAssessmentList();
		for (StudentCourse sc : studentCourseList) {
			if (!sc.isDuplicate() && !sc.isFailed() && !sc.isNotCompleted() && !sc.isProjected() && !sc.isLessCreditCourse()) {
				TranscriptResult result = new TranscriptResult();
				String equivOrChallenge = "";
				if (sc.getEquivOrChallenge() != null) {
					equivOrChallenge = sc.getEquivOrChallenge();
				}
				Course crse = new Course();
				crse.setCode(sc.getCourseCode());
				crse.setCredits(sc.getCredits().toString());
				crse.setLevel(sc.getCourseLevel());
				crse.setName(getCourseNameLogic(sc));
				crse.setRelatedCourse(sc.getRelatedCourse());
				crse.setRelatedLevel(sc.getRelatedLevel());
				crse.setType(equivOrChallenge.equals("E") ? "1" : "2");
				crse.setSessionDate(sc.getSessionDate() != null ? sc.getSessionDate().replace("/", "") : "");
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
				result.setUsedForGrad(sc.getCreditsUsedForGrad() != null ? sc.getCreditsUsedForGrad().toString() : "");
				result.setRequirementName(sc.getGradReqMetDetail());
				result.setEquivalency(equivOrChallenge);
				tList.add(result);
			}
		}

		for (StudentAssessment sc : studentAssessmentList) {
			if (!sc.isDuplicate() && !sc.isFailed() && !sc.isNotCompleted() && !sc.isProjected()) {
				TranscriptResult result = new TranscriptResult();
				Course crse = new Course();
				crse.setCode(sc.getAssessmentCode());
				crse.setLevel("");
				crse.setCredits("NA");
				crse.setName(sc.getAssessmentName());
				crse.setSessionDate(sc.getSessionDate() != null ? sc.getSessionDate().replace("/", "") : "");
				result.setCourse(crse);

				Mark mrk = new Mark();

				mrk.setExamPercent("");
				mrk.setFinalLetterGrade("");
				mrk.setInterimLetterGrade("");
				mrk.setInterimPercent("");
				mrk.setSchoolPercent("");
				mrk.setFinalLetterGrade("NA");
				mrk.setFinalPercent(getAssessmentFinalPercent(sc, accessToken));
				result.setMark(mrk);
				result.setRequirement(sc.getGradReqMet());
				result.setRequirementName(sc.getGradReqMetDetail());
				tList.add(result);
			}
		}

		return tList;
	}

	private String getAssessmentFinalPercent(StudentAssessment sA, String accessToken) {
		String finalPercent;
		if (sA.getSpecialCase() != null && StringUtils.isNotBlank(sA.getSpecialCase().trim())) {
			finalPercent = sA.getSpecialCase();
			if (sA.getSpecialCase().equalsIgnoreCase("A") || sA.getSpecialCase().equalsIgnoreCase("E")) {
				SpecialCase spC = webClient.get().uri(String.format(educGraduationApiConstants.getSpecialCase(), sA.getSpecialCase())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(SpecialCase.class).block();
				finalPercent = spC != null ? spC.getLabel():"";
			}
		} else {
			finalPercent = sA.getProficiencyScore() != null ? new DecimalFormat("#").format(sA.getProficiencyScore()) : "";
			if (sA.getAssessmentCode().equalsIgnoreCase("LTE10") || sA.getAssessmentCode().equalsIgnoreCase("LTP10")) {
				finalPercent = sA.getProficiencyScore() != null ? sA.getProficiencyScore().toString() : "RM";
			}
		}
		return finalPercent;
	}

	private String getCourseNameLogic(StudentCourse sc) {
		if (sc.getGenericCourseType() != null && sc.getGenericCourseType().equalsIgnoreCase("I") && StringUtils.isNotBlank(sc.getRelatedCourse()) && StringUtils.isNotBlank(sc.getRelatedLevel()) && StringUtils.isNotBlank(sc.getRelatedCourseName())) {
			return "IDS " + sc.getRelatedCourseName();
		}
		if (StringUtils.isNotBlank(sc.getCustomizedCourseName())) {
			return sc.getCustomizedCourseName();
		}
		return sc.getCourseName();
	}

	private String getValue(Double value) {
		return value != null && value != 0.0 ? new DecimalFormat("#").format(value) : "";
	}

	private GraduationData getGraduationData(
			ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus) {
		GraduationData data = new GraduationData();
		data.setDogwoodFlag(graduationDataStatus.isDualDogwood());
		if (graduationDataStatus.isGraduated()) {
			if (!graduationDataStatus.getGradStatus().getProgram().equalsIgnoreCase("SCCP")) {
				if (graduationDataStatus.getGradStatus().getProgramCompletionDate() != null) {
					if (graduationDataStatus.getGradStatus().getProgramCompletionDate().length() > 7) {
						data.setGraduationDate(graduationDataStatus.getGradStatus().getProgramCompletionDate());
					} else {
						data.setGraduationDate(EducGraduationApiUtils.parsingNFormating(graduationDataStatus.getGradStatus().getProgramCompletionDate()));
					}
				}
				data.setHonorsFlag(graduationDataStatus.getGradStatus().getHonoursStanding().equals("Y"));
			} else {
				data.setGraduationDate(EducGraduationApiUtils.parsingNFormating(graduationDataStatus.getGradStatus().getProgramCompletionDate()));
			}
		}

		return data;
	}

	private GradProgram getGradProgram(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, String accessToken) {
		GradProgram gPgm = new GradProgram();
		Code code = new Code();
		if (graduationDataStatus.getGradStatus().getProgram() != null) {
			ca.bc.gov.educ.api.graduation.model.dto.GradProgram gradProgram = webClient.get().uri(String.format(educGraduationApiConstants.getProgramNameEndpoint(), graduationDataStatus.getGradStatus().getProgram())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(ca.bc.gov.educ.api.graduation.model.dto.GradProgram.class).block();
			if(gradProgram != null) {
				code.setDescription(gradProgram.getProgramName());
				code.setName(gradProgram.getProgramName());
			}
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


	private ca.bc.gov.educ.api.graduation.model.report.School getSchoolData(ca.bc.gov.educ.api.graduation.model.dto.School school) {
		ca.bc.gov.educ.api.graduation.model.report.School schObj = new ca.bc.gov.educ.api.graduation.model.report.School();
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
		schObj.setDistno(school.getMinCode().substring(0, 3));
		schObj.setSchlno(school.getMinCode());
		return schObj;
	}

	private ca.bc.gov.educ.api.graduation.model.achvreport.School getSchoolDataAchvReport(ca.bc.gov.educ.api.graduation.model.dto.School school) {
		ca.bc.gov.educ.api.graduation.model.achvreport.School schObj = new ca.bc.gov.educ.api.graduation.model.achvreport.School();
		schObj.setMincode(school.getMinCode());
		schObj.setSchoolName(school.getSchoolName());
		return schObj;
	}

	private GradStatus getGraduationStatus(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationData) {
		GradStatus gradStatus = new GradStatus();
		gradStatus.setGraduationMessage(graduationData.getGradMessage());
		return gradStatus;
	}

	private List<NonGraduationReason> getNonGradReasonsAchvReport(List<GradRequirement> nonGradReasons) {
		List<NonGraduationReason> nList = new ArrayList<>();
		if (nonGradReasons != null) {
			for (GradRequirement gR : nonGradReasons) {
				NonGraduationReason obj = new NonGraduationReason();
				obj.setRule(gR.getRule());
				obj.setDescription(gR.getDescription());
				nList.add(obj);
			}
		}
		return nList;
	}

	private ca.bc.gov.educ.api.graduation.model.achvreport.Student getStudentDataAchvReport(GradSearchStudent studentObj, List<StudentOptionalProgram> optionalStudentProgram) {
		ca.bc.gov.educ.api.graduation.model.achvreport.Student studObj = new ca.bc.gov.educ.api.graduation.model.achvreport.Student();
		studObj.setGender(StudentGenderEnum.valueOf(studentObj.getGenderCode()).toString());
		studObj.setFirstName(studentObj.getLegalFirstName());
		studObj.setMiddleName(studentObj.getLegalMiddleNames() != null ?studentObj.getLegalMiddleNames():"");
		studObj.setLastName(studentObj.getLegalLastName());
		studObj.setGrade(studentObj.getStudentGrade());
		studObj.setPen(studentObj.getPen());
		studObj.setLocalId(studentObj.getLocalID());
		studObj.setProgram(studentObj.getProgram());
		studObj.setBirthdate(studentObj.getDob());
		List<OtherProgram> otherProgramParticipation = new ArrayList<>();
		for (StudentOptionalProgram sp : optionalStudentProgram) {
			OtherProgram op = new OtherProgram();
			op.setProgramCode(sp.getOptionalProgramCode());
			op.setProgramName(sp.getOptionalProgramName());
			otherProgramParticipation.add(op);
		}
		if (!otherProgramParticipation.isEmpty()) {
			studObj.setOtherProgramParticipation(otherProgramParticipation);
			studObj.setHasOtherProgram("Other Program Participation");
		}else {
			studObj.setHasOtherProgram(" ");
		}
		return studObj;
	}

	private void getStudentCoursesAssessmentsNExams(AchvReportData data, ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus) {
		List<StudentCourse> studentCourseList = graduationDataStatus.getStudentCourses().getStudentCourseList();
		List<StudentCourse> studentExamList = studentCourseList
				.stream()
				.filter(sc -> "Y".compareTo(sc.getProvExamCourse()) == 0)
				.collect(Collectors.toList());
		List<StudentAssessment> studentAssessmentList = graduationDataStatus.getStudentAssessments().getStudentAssessmentList();
		List<StudCourse> sCourseList = new ArrayList<>();
		List<StudAssessment> sAssessmentList = new ArrayList<>();
		List<StudExam> sExamList = new ArrayList<>();
		for (StudentCourse sc : studentCourseList) {
			StudCourse crse = new StudCourse();
			String equivOrChallenge = "";
			if (sc.getEquivOrChallenge() != null) {
				equivOrChallenge = sc.getEquivOrChallenge();
			}
			crse.setCourseCode(sc.getCourseCode());
			crse.setCredits(sc.getCredits().toString());
			crse.setCourseLevel(sc.getCourseLevel());
			crse.setCourseName(getCourseNameLogic(sc));
			crse.setSessionDate(sc.getSessionDate() != null ? sc.getSessionDate(): "");
			crse.setCompletedCourseLetterGrade(sc.getCompletedCourseLetterGrade());
			crse.setCompletedCoursePercentage(getValue(sc.getCompletedCoursePercentage()));
			crse.setGradReqMet(sc.getGradReqMet());
			crse.setCreditsUsedForGrad(sc.getCreditsUsedForGrad() != null ? sc.getCreditsUsedForGrad() : 0);
			crse.setEquivOrChallenge(equivOrChallenge);
			sCourseList.add(crse);
		}

		for(StudentAssessment sA:studentAssessmentList)	{

			StudAssessment achv = new StudAssessment();
			achv.setAssessmentCode(sA.getAssessmentCode());
			achv.setAssessmentName(sA.getAssessmentName());
			achv.setGradReqMet(sA.getGradReqMet());
			achv.setSessionDate(sA.getSessionDate() != null ? sA.getSessionDate(): "");
			achv.setProficiencyScore(sA.getProficiencyScore());
			achv.setSpecialCase(sA.getSpecialCase());
			achv.setExceededWriteFlag(sA.getExceededWriteFlag());
			sAssessmentList.add(achv);
		}

		for (StudentCourse sc : studentExamList) {
			StudExam crse = new StudExam();
			String equivOrChallenge = "";
			if (sc.getEquivOrChallenge() != null) {
				equivOrChallenge = sc.getEquivOrChallenge();
			}
			crse.setCourseCode(sc.getCourseCode());
			crse.setCredits(sc.getCredits().toString());
			crse.setCourseLevel(sc.getCourseLevel());
			crse.setCourseName(getCourseNameLogic(sc));
			crse.setSessionDate(sc.getSessionDate() != null ? sc.getSessionDate(): "");
			crse.setCompletedCourseLetterGrade(sc.getCompletedCourseLetterGrade());
			crse.setCompletedCoursePercentage(getValue(sc.getCompletedCoursePercentage()));
			crse.setGradReqMet(sc.getGradReqMet());
			crse.setCreditsUsedForGrad(sc.getCreditsUsedForGrad() != null ? sc.getCreditsUsedForGrad() : 0);
			crse.setEquivOrChallenge(equivOrChallenge);
			crse.setBestSchoolPercent(getValue(sc.getBestSchoolPercent()));
			crse.setBestExamPercent(getValue(sc.getBestExamPercent()));
			crse.setMetLitNumRequirement(sc.getMetLitNumRequirement());
			sExamList.add(crse);
		}
		if (!sCourseList.isEmpty()) {
			Collections.sort(sCourseList, Comparator.comparing(StudCourse::getCourseCode)
					.thenComparing(StudCourse::getCourseLevel)
					.thenComparing(StudCourse::getSessionDate));
		}
		if (!sExamList.isEmpty()) {
			Collections.sort(sExamList, Comparator.comparing(StudExam::getCourseCode)
					.thenComparing(StudExam::getCourseLevel)
					.thenComparing(StudExam::getSessionDate));
		}
		if (!sAssessmentList.isEmpty()) {
			Collections.sort(sAssessmentList, Comparator.comparing(StudAssessment::getAssessmentCode)
					.thenComparing(StudAssessment::getSessionDate));
		}
		data.setStudentAssessments(sAssessmentList);
		data.setStudentCourses(sCourseList);
		data.setStudentExams(sExamList);
	}

	public void saveStudentTranscriptReportJasper(String pen,
			ca.bc.gov.educ.api.graduation.model.report.ReportData sample, String accessToken, UUID studentID,ExceptionMessage exception,boolean isGraduated) {
	
		String encodedPdfReportTranscript = generateStudentTranscriptReportJasper(sample,accessToken,exception);
		GradStudentReports requestObj = new GradStudentReports();
		requestObj.setPen(pen);
		requestObj.setReport(encodedPdfReportTranscript);
		requestObj.setStudentID(studentID);
		requestObj.setGradReportTypeCode("TRAN");
		requestObj.setDocumentStatusCode("IP");
		if(isGraduated)
			requestObj.setDocumentStatusCode(DOCUMENT_STATUS_COMPLETED);
		
		try {
			webClient.post().uri(String.format(educGraduationApiConstants.getUpdateGradStudentReport(),isGraduated)).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(requestObj)).retrieve().bodyToMono(GradStudentReports.class).block();
		}catch(Exception e) {
			if(exception.getExceptionName() == null) {
				exception.setExceptionName(GRAD_GRADUATION_REPORT_API_DOWN);
				exception.setExceptionDetails(e.getLocalizedMessage());
			}
		}
		
	}

	private String generateStudentTranscriptReportJasper(ca.bc.gov.educ.api.graduation.model.report.ReportData sample,
			String accessToken,ExceptionMessage exception) {
		ReportOptions options = new ReportOptions();
		options.setReportFile("transcript");
		options.setReportName("Transcript Report.pdf");
		ReportRequest reportParams = new ReportRequest();
		reportParams.setOptions(options);
		reportParams.setData(sample);
		try {
		byte[] bytesSAR = webClient.post().uri(educGraduationApiConstants.getTranscriptReport()).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(reportParams)).retrieve().bodyToMono(byte[].class).block();
		byte[] encoded = Base64.encodeBase64(bytesSAR);
	    return new String(encoded,StandardCharsets.US_ASCII);
		}catch (Exception e) {
			exception.setExceptionName(GRAD_REPORT_API_DOWN);
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
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

	public void saveStudentCertificateReportJasper(GraduationStudentRecord gradResponse,ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, String accessToken,
			ProgramCertificateTranscript certType,ExceptionMessage exception) {
		ReportData certData = prepareCertificateData(graduationDataStatus,accessToken);
		certData.setUpdateDate(EducGraduationApiUtils.formatDateForReportJasper(gradResponse.getUpdateDate().toString()));
		certData.setCertificate(getCertificateData(gradResponse,certType));
		if(certType.getCertificateTypeCode().equalsIgnoreCase("E") || certType.getCertificateTypeCode().equalsIgnoreCase("A") || certType.getCertificateTypeCode().equalsIgnoreCase("EI") || certType.getCertificateTypeCode().equalsIgnoreCase("AI")) {
			certData.getStudent().setEnglishCert(certType.getCertificateTypeCode());
		}else if(certType.getCertificateTypeCode().equalsIgnoreCase("F") || certType.getCertificateTypeCode().equalsIgnoreCase("S")) {
			certData.getStudent().setFrenchCert(certType.getCertificateTypeCode());
		}
		String encodedPdfReportCertificate = generateStudentCertificateReportJasper(certData,accessToken,exception);
		GradStudentCertificates requestObj = new GradStudentCertificates();
		requestObj.setPen(gradResponse.getPen());
		requestObj.setStudentID(gradResponse.getStudentID());
		requestObj.setCertificate(encodedPdfReportCertificate);
		requestObj.setGradCertificateTypeCode(certType.getCertificateTypeCode());
		requestObj.setDocumentStatusCode(DOCUMENT_STATUS_COMPLETED);
		webClient.post().uri(educGraduationApiConstants.getUpdateGradStudentCertificate()).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(requestObj)).retrieve().bodyToMono(GradStudentCertificates.class).block();

	}
	
	private Certificate getCertificateData(GraduationStudentRecord gradResponse,ProgramCertificateTranscript certData) {
		Certificate cert = new Certificate();
		cert.setIssued(EducGraduationApiUtils.formatDateForReportJasper(EducGraduationApiUtils.parsingDateForCertificate(gradResponse.getProgramCompletionDate())));
		OrderType orTy = new OrderType();
		orTy.setName("Certificate");
		CertificateType certType = new CertificateType();
		PaperType pType = new PaperType();
		pType.setCode("YEDR");
		certType.setPaperType(pType);
		certType.setReportName(certData.getCertificateTypeCode());
		orTy.setCertificateType(certType);
		cert.setOrderType(orTy);
		return cert;
	}

	private String generateStudentCertificateReportJasper(ca.bc.gov.educ.api.graduation.model.report.ReportData sample,
			String accessToken,ExceptionMessage exception) {
		ReportOptions options = new ReportOptions();
		options.setReportFile("certificate");
		options.setReportName("Certificate.pdf");
		ReportRequest reportParams = new ReportRequest();
		reportParams.setOptions(options);
		reportParams.setData(sample);		
		try{
		byte[] bytesSAR = webClient.post().uri(educGraduationApiConstants.getCertificateReport()).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(reportParams)).retrieve().bodyToMono(byte[].class).block();
		byte[] encoded = Base64.encodeBase64(bytesSAR);
	    return new String(encoded,StandardCharsets.US_ASCII);
		}catch (Exception e) {
			exception.setExceptionName(GRAD_REPORT_API_DOWN);
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
	}
	
	private String generateStudentAchievementReportJasper(AchvReportData data, String accessToken,ExceptionMessage exception) {
		AchvReportOptions options = new AchvReportOptions();
		options.setReportFile("achievement");
		options.setReportName("Student Achievement Report.pdf");
		GenerateReportData reportParams = new GenerateReportData();
		reportParams.setOptions(options);
		reportParams.setData(data);
		try {
		byte[] bytesSAR = webClient.post().uri(educGraduationApiConstants.getAchievementReport()).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(reportParams)).retrieve().bodyToMono(byte[].class).block();
		byte[] encoded = Base64.encodeBase64(bytesSAR);
	    return new String(encoded,StandardCharsets.US_ASCII);
		}catch (Exception e) {
			exception.setExceptionName(GRAD_REPORT_API_DOWN);
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}

	}

	public AchvReportData prepareAchievementReportData(
			ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, List<StudentOptionalProgram> optionalProgramList) {
		AchvReportData data = new AchvReportData();
		data.setSchool(getSchoolDataAchvReport(graduationDataStatus.getSchool()));
		data.setStudent(getStudentDataAchvReport(graduationDataStatus.getGradStudent(),optionalProgramList));
		data.setOrgCode(StringUtils.startsWith(data.getSchool().getMincode(), "098") ? "YU":"BC");
		data.setGraduationStatus(getGraduationStatus(graduationDataStatus));
		getStudentCoursesAssessmentsNExams(data,graduationDataStatus);
		data.setNonGradReasons(getNonGradReasonsAchvReport(graduationDataStatus.getNonGradReasons()));
		data.setOptionalPrograms(getOptionalProgramAchvReport(optionalProgramList));
		return data;
	}

	private List<OptionalProgram> getOptionalProgramAchvReport(List<StudentOptionalProgram> optionalProgramList) {
		List<OptionalProgram> opList = new ArrayList<>();
		for(StudentOptionalProgram sPO:optionalProgramList) {
			OptionalProgram op = new OptionalProgram();
			op.setOptionalProgramCode(sPO.getOptionalProgramCode());
			op.setOptionalProgramName(sPO.getOptionalProgramName());
			op.setProgramCompletionDate(sPO.getOptionalProgramCompletionDate());

			GradAlgorithmOptionalStudentProgram existingData=null;
			try {
				existingData = new ObjectMapper().readValue(sPO.getStudentOptionalProgramData(), GradAlgorithmOptionalStudentProgram.class);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			if(existingData != null && existingData.getOptionalNonGradReasons() != null) {
				op.setNonGradReasons(getNonGradReasonsAchvReport(existingData.getOptionalNonGradReasons()));
			}
			op.setHasRequirementMet(" Check with School");
			if(existingData != null && existingData.getOptionalRequirementsMet() != null) {
				op.setHasRequirementMet(null);
				op.setRequirementMet(getRequirementsMetAchvReport(existingData.getOptionalRequirementsMet(),existingData.getOptionalStudentCourses()));
			}
			opList.add(op);
		}
		return opList;
	}

	private List<GraduationRequirement> getRequirementsMetAchvReport(List<GradRequirement> optionalRequirementsMet, StudentCourses optionalStudentCourses) {
		List<GraduationRequirement> grList = new ArrayList<>();
		for(GradRequirement gr:optionalRequirementsMet) {
			GraduationRequirement gRAchv = new GraduationRequirement();
			gRAchv.setRule(gr.getRule());
			gRAchv.setDescription(gr.getDescription());

			List<StudentCourse> scList = optionalStudentCourses.getStudentCourseList()
					.stream()
					.filter(sc -> sc.getGradReqMet().contains(gr.getRule()))
					.collect(Collectors.toList());
			List<CourseDetails> cdList = new ArrayList<>();
			scList.forEach(sc->{
				CourseDetails cD = new CourseDetails();
				cD.setCourseCode(sc.getCourseCode());
				cD.setCourseLevel(sc.getCourseLevel());
				cD.setSessionDate(sc.getSessionDate());
				cdList.add(cD);
			});

			gRAchv.setCourseDetails(cdList);
			grList.add(gRAchv);
		}
		return  grList;
	}

	public void saveStudentAchivementReportJasper(String pen,AchvReportData sample, String accessToken, UUID studentID,ExceptionMessage exception,boolean isGraduated) {
		String encodedPdfReportTranscript = generateStudentAchievementReportJasper(sample,accessToken,exception);
		GradStudentReports requestObj = new GradStudentReports();
		requestObj.setPen(pen);
		requestObj.setReport(encodedPdfReportTranscript);
		requestObj.setStudentID(studentID);
		requestObj.setGradReportTypeCode("ACHV");
		requestObj.setDocumentStatusCode("IP");
		if(isGraduated)
			requestObj.setDocumentStatusCode(DOCUMENT_STATUS_COMPLETED);

		try {
			webClient.post().uri(String.format(educGraduationApiConstants.getUpdateGradStudentReport(),isGraduated)).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(requestObj)).retrieve().bodyToMono(GradStudentReports.class).block();
		}catch(Exception e) {
			if(exception.getExceptionName() == null) {
				exception.setExceptionName(GRAD_GRADUATION_REPORT_API_DOWN);
				exception.setExceptionDetails(e.getLocalizedMessage());
			}
		}

	}
}
