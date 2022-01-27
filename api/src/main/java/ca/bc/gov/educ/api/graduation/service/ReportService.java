package ca.bc.gov.educ.api.graduation.service;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.dto.StudentAssessment;
import ca.bc.gov.educ.api.graduation.model.dto.StudentCourse;
import ca.bc.gov.educ.api.graduation.model.report.*;
import ca.bc.gov.educ.api.graduation.model.report.GradProgram;
import ca.bc.gov.educ.api.graduation.model.report.GradRequirement;
import ca.bc.gov.educ.api.graduation.model.report.GraduationData;
import ca.bc.gov.educ.api.graduation.model.report.GraduationStatus;
import ca.bc.gov.educ.api.graduation.model.report.School;
import ca.bc.gov.educ.api.graduation.model.report.Student;
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

	public ProgramCertificateTranscript getTranscript(GraduationStudentRecord gradResponse, ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, String accessToken, ExceptionMessage exception) {
		ProgramCertificateReq req = new ProgramCertificateReq();
		req.setProgramCode(gradResponse.getProgram());
		req.setSchoolCategoryCode(getSchoolCategoryCode(accessToken, graduationDataStatus.getGradStatus().getSchoolOfRecord()));
		try {
			return webClient.post().uri(educGraduationApiConstants.getTranscript()).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(req)).retrieve().bodyToMono(ProgramCertificateTranscript.class).block();
		} catch (Exception e) {
			exception.setExceptionName(GRAD_GRADUATION_REPORT_API_DOWN);
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
	}
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
			ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, GraduationStudentRecord gradResponse, String accessToken,ExceptionMessage exception) {
		ReportData data = new ReportData();
		data.setSchool(getSchoolData(graduationDataStatus.getSchool()));
		data.setStudent(getStudentData(graduationDataStatus.getGradStudent()));
		data.setGradMessage(graduationDataStatus.getGradMessage());
		data.setGradProgram(getGradProgram(graduationDataStatus, accessToken));
		data.setGraduationData(getGraduationData(graduationDataStatus));
		data.setLogo(StringUtils.startsWith(data.getSchool().getMincode(), "098") ? "YU" : "BC");
		data.setTranscript(getTranscriptData(graduationDataStatus, gradResponse, accessToken,exception));
		data.setNonGradReasons(getNonGradReasons(graduationDataStatus.getNonGradReasons()));
		return data;
	}

	private List<NonGradReason> getNonGradReasons(List<ca.bc.gov.educ.api.graduation.model.dto.GradRequirement> nonGradReasons) {
		List<NonGradReason> nList = new ArrayList<>();
		if (nonGradReasons != null) {
			for (ca.bc.gov.educ.api.graduation.model.dto.GradRequirement gR : nonGradReasons) {
				NonGradReason obj = new NonGradReason();
				obj.setCode(gR.getRule());
				obj.setDescription(gR.getDescription());
				nList.add(obj);
			}
		}
		return nList;
	}

	private Transcript getTranscriptData(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, GraduationStudentRecord gradResponse, String accessToken,ExceptionMessage exception) {
		Transcript transcriptData = new Transcript();
		transcriptData.setInterim("false");
		ProgramCertificateTranscript pcObj = getTranscript(gradResponse,graduationDataStatus,accessToken,exception);
		if(pcObj != null) {
			Code code = new Code();
			code.setCode(pcObj.getTranscriptTypeCode());
			transcriptData.setTranscriptTypeCode(code);
		}
		transcriptData.setIssueDate(EducGraduationApiUtils.formatIssueDateForReportJasper(gradResponse.getUpdateDate().toString()));
		transcriptData.setResults(getTranscriptResults(graduationDataStatus, accessToken));
		return transcriptData;
	}

	private List<TranscriptResult> getTranscriptResults(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, String accessToken) {
		List<TranscriptResult> tList = new ArrayList<>();
		List<StudentCourse> studentCourseList = graduationDataStatus.getStudentCourses().getStudentCourseList();
		List<StudentAssessment> studentAssessmentList = graduationDataStatus.getStudentAssessments().getStudentAssessmentList();
		for (StudentCourse sc : studentCourseList) {
			if (!sc.isDuplicate() && !sc.isFailed() && !sc.isNotCompleted() && !sc.isProjected() && !sc.isLessCreditCourse() && !sc.isValidationCourse() && !sc.isGrade10Course() && !sc.isCutOffCourse()) {
				TranscriptResult result = new TranscriptResult();
				String equivOrChallenge = "";
				if (sc.getEquivOrChallenge() != null) {
					equivOrChallenge = sc.getEquivOrChallenge();
				}
				TranscriptCourse crse = new TranscriptCourse();
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
				TranscriptCourse crse = new TranscriptCourse();
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

	private ca.bc.gov.educ.api.graduation.model.report.GraduationData getGraduationData(
			ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus) {
		GraduationData data = new GraduationData();
		data.setDogwoodFlag(graduationDataStatus.isDualDogwood());
		if (graduationDataStatus.isGraduated()) {
			if (!graduationDataStatus.getGradStatus().getProgram().equalsIgnoreCase("SCCP")) {
				if (graduationDataStatus.getGradStatus().getProgramCompletionDate() != null) {
					if (graduationDataStatus.getGradStatus().getProgramCompletionDate().length() > 7) {
						data.setGraduationDate(EducGraduationApiUtils.formatIssueDateForReportJasper(graduationDataStatus.getGradStatus().getProgramCompletionDate()));
					} else {
						data.setGraduationDate(EducGraduationApiUtils.formatIssueDateForReportJasper(EducGraduationApiUtils.parsingNFormating(graduationDataStatus.getGradStatus().getProgramCompletionDate())));
					}
				}
				data.setHonorsFlag(graduationDataStatus.getGradStatus().getHonoursStanding().equals("Y"));
			} else {
				data.setGraduationDate(EducGraduationApiUtils.formatIssueDateForReportJasper(EducGraduationApiUtils.parsingNFormating(graduationDataStatus.getGradStatus().getProgramCompletionDate())));
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
		schObj.setDistno(school.getMinCode().substring(0, 3));
		schObj.setSchlno(school.getMinCode());
		return schObj;
	}

	private School getSchoolDataAchvReport(ca.bc.gov.educ.api.graduation.model.dto.School school) {
		School schObj = new School();
		schObj.setMincode(school.getMinCode());
		schObj.setName(school.getSchoolName());
		return schObj;
	}

	private GraduationStatus getGraduationStatus(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationData) {
		GraduationStatus gradStatus = new GraduationStatus();
		gradStatus.setGraduationMessage(graduationData.getGradMessage());
		return gradStatus;
	}

	private Student getStudentDataAchvReport(GradSearchStudent studentObj, List<StudentOptionalProgram> optionalStudentProgram) {
		Student studObj = new Student();
		studObj.setGender(StudentGenderEnum.valueOf(studentObj.getGenderCode()).toString());
		studObj.setFirstName(studentObj.getLegalFirstName());
		studObj.setLastName(studentObj.getLegalLastName());
		studObj.setGrade(studentObj.getStudentGrade());
		Pen pen = new Pen();
		pen.setPen(studentObj.getPen());
		studObj.setPen(pen);
		studObj.setLocalId(studentObj.getLocalID());
		studObj.setGradProgram(studentObj.getProgram());
		studObj.setBirthdate(EducGraduationApiUtils.formatIssueDateForReportJasper(studentObj.getDob()));
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

	private void getStudentCoursesAssessmentsNExams(ReportData data, ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus) {
		List<StudentCourse> studentCourseList = graduationDataStatus.getStudentCourses().getStudentCourseList();
		List<StudentCourse> studentExamList = studentCourseList
				.stream()
				.filter(sc -> "Y".compareTo(sc.getProvExamCourse()) == 0)
				.collect(Collectors.toList());
		List<StudentAssessment> studentAssessmentList = graduationDataStatus.getStudentAssessments().getStudentAssessmentList();
		List<AchievementCourse> sCourseList = new ArrayList<>();
		List<Exam> sExamList = new ArrayList<>();
		for (StudentCourse sc : studentCourseList) {
			AchievementCourse crse = new AchievementCourse();
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
			crse.setUsedForGrad(sc.getCreditsUsedForGrad() != null ? sc.getCreditsUsedForGrad().toString() : "0");
			crse.setEquivOrChallenge(equivOrChallenge);
			sCourseList.add(crse);
		}
		if (!sCourseList.isEmpty()) {
			Collections.sort(sCourseList, Comparator.comparing(AchievementCourse::getCourseCode)
					.thenComparing(AchievementCourse::getCourseLevel)
					.thenComparing(AchievementCourse::getSessionDate));
		}
		data.setStudentCourses(sCourseList);

		Assessment achv = new Assessment();
		achv.setIssueDate(EducGraduationApiUtils.formatIssueDateForReportJasper(EducGraduationApiUtils.getSimpleDateFormat(new Date())));
		achv.setResults(getAssessmentResults(studentAssessmentList));
		data.setAssessment(achv);

		for (StudentCourse sc : studentExamList) {
			Exam crse = new Exam();
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
			crse.setMetLitNumRequirement(sc.getMetLitNumRequirement() != null?sc.getMetLitNumRequirement():"");
			sExamList.add(crse);
		}

		if (!sExamList.isEmpty()) {
			Collections.sort(sExamList, Comparator.comparing(Exam::getCourseCode)
					.thenComparing(Exam::getCourseLevel)
					.thenComparing(Exam::getSessionDate));
		}
		data.setStudentExams(sExamList);
	}
	private List<AssessmentResult> getAssessmentResults(List<StudentAssessment> studentAssessmentList) {
		List<AssessmentResult> tList = new ArrayList<>();
		for (StudentAssessment sA : studentAssessmentList) {
			AssessmentResult result = new AssessmentResult();
			result.setAssessmentCode(sA.getAssessmentCode());
			result.setAssessmentName(sA.getAssessmentName());
			result.setGradReqMet(sA.getGradReqMet());
			result.setSessionDate(sA.getSessionDate() != null ? sA.getSessionDate(): "");
			result.setProficiencyScore(sA.getProficiencyScore() != null? sA.getProficiencyScore():null);
			result.setSpecialCase(sA.getSpecialCase());
			result.setExceededWriteFlag(sA.getExceededWriteFlag());
			tList.add(result);
		}
		if (!tList.isEmpty()) {
			Collections.sort(tList, Comparator.comparing(AssessmentResult::getAssessmentCode)
					.thenComparing(AssessmentResult::getSessionDate));
		}
		return tList;
	}
	public void saveStudentTranscriptReportJasper(String pen,
			ReportData sample, String accessToken, UUID studentID,ExceptionMessage exception,boolean isGraduated) {
	
		String encodedPdfReportTranscript = generateStudentTranscriptReportJasper(sample,accessToken,exception);
		GradStudentTranscripts requestObj = new GradStudentTranscripts();
		requestObj.setTranscript(encodedPdfReportTranscript);
		requestObj.setStudentID(studentID);
		requestObj.setTranscriptTypeCode(sample.getTranscript().getTranscriptTypeCode().getCode());
		requestObj.setDocumentStatusCode("IP");
		if(isGraduated)
			requestObj.setDocumentStatusCode(DOCUMENT_STATUS_COMPLETED);
		
		try {
			webClient.post().uri(String.format(educGraduationApiConstants.getUpdateGradStudentTranscript(),isGraduated)).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(requestObj)).retrieve().bodyToMono(GradStudentReports.class).block();
		}catch(Exception e) {
			if(exception.getExceptionName() == null) {
				exception.setExceptionName(GRAD_GRADUATION_REPORT_API_DOWN);
				exception.setExceptionDetails(e.getLocalizedMessage());
			}
		}
		
	}

	private String generateStudentTranscriptReportJasper(ReportData sample,
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
		ReportData data = new ReportData();
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
		cert.setIssued(EducGraduationApiUtils.formatIssueDateForReportJasper(EducGraduationApiUtils.parsingDateForCertificate(gradResponse.getProgramCompletionDate())));
		OrderType orTy = new OrderType();
		orTy.setName("Certificate");
		CertificateType certType = new CertificateType();
		PaperType pType = new PaperType();
		pType.setCode(certData.getCertificatePaperType());
		certType.setPaperType(pType);
		certType.setReportName(certData.getCertificateTypeCode());
		orTy.setCertificateType(certType);
		cert.setOrderType(orTy);
		cert.setCertStyle("Original");
		return cert;
	}

	private String generateStudentCertificateReportJasper(ReportData sample,
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
	
	private String generateStudentAchievementReportJasper(ReportData data, String accessToken,ExceptionMessage exception) {
		ReportOptions options = new ReportOptions();
		options.setReportFile("achievement");
		options.setReportName("Student Achievement Report.pdf");
		ReportRequest reportParams = new ReportRequest();
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

	public ReportData prepareAchievementReportData(
			ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, List<StudentOptionalProgram> optionalProgramList) {
		ReportData data = new ReportData();
		data.setSchool(getSchoolDataAchvReport(graduationDataStatus.getSchool()));
		data.setStudent(getStudentDataAchvReport(graduationDataStatus.getGradStudent(),optionalProgramList));
		data.setOrgCode(StringUtils.startsWith(data.getSchool().getMincode(), "098") ? "YU":"BC");
		data.setGraduationStatus(getGraduationStatus(graduationDataStatus));
		getStudentCoursesAssessmentsNExams(data,graduationDataStatus);
		data.setNonGradReasons(getNonGradReasons(graduationDataStatus.getNonGradReasons()));
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
				op.setNonGradReasons(getNonGradReasons(existingData.getOptionalNonGradReasons()));
			}
			op.setHasRequirementMet(" Check with School");
			if(existingData != null && existingData.getOptionalRequirementsMet() != null) {
				op.setHasRequirementMet("The Following Requirements Are Met");
				op.setRequirementMet(getRequirementsMetAchvReport(existingData.getOptionalRequirementsMet(),existingData.getOptionalStudentCourses()));
			}
			opList.add(op);
		}
		return opList;
	}

	private List<GradRequirement> getRequirementsMetAchvReport(List<ca.bc.gov.educ.api.graduation.model.dto.GradRequirement> optionalRequirementsMet, StudentCourses optionalStudentCourses) {
		List<GradRequirement> grList = new ArrayList<>();
		for(ca.bc.gov.educ.api.graduation.model.dto.GradRequirement gr:optionalRequirementsMet) {
			GradRequirement gRAchv = new GradRequirement();
			gRAchv.setCode(gr.getRule());
			gRAchv.setDescription(gr.getDescription());

			List<StudentCourse> scList = optionalStudentCourses.getStudentCourseList()
					.stream()
					.filter(sc -> sc.getGradReqMet().contains(gr.getRule()))
					.collect(Collectors.toList());
			List<AchievementCourse> cdList = new ArrayList<>();
			scList.forEach(sc->{
				AchievementCourse cD = new AchievementCourse();
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

	public void saveStudentAchivementReportJasper(String pen,ReportData sample, String accessToken, UUID studentID,ExceptionMessage exception,boolean isGraduated) {
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
