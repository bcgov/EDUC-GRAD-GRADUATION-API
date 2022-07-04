package ca.bc.gov.educ.api.graduation.service;


import ca.bc.gov.educ.api.graduation.model.dto.GradRequirement;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.School;
import ca.bc.gov.educ.api.graduation.model.report.*;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcess;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcessFactory;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcessType;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiUtils;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import ca.bc.gov.educ.api.graduation.util.ThreadLocalStateUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class GraduationService {

	private static Logger logger = LoggerFactory.getLogger(GraduationService.class);

	@Autowired
    WebClient webClient;
	
	@Autowired
	AlgorithmProcessFactory algorithmProcessFactory;
	
	@Autowired
	GradStatusService gradStatusService;

	@Autowired
	SchoolService schoolService;
	
	@Autowired
	GradAlgorithmService gradAlgorithmService;
	
	@Autowired
	OptionalProgramService optionalProgramService;
	
	@Autowired
	ReportService reportService;
	
	@Autowired
	GradValidation validation;

	@Autowired
	EducGraduationApiConstants educGraduationApiConstants;

	public AlgorithmResponse graduateStudent(String studentID, Long batchId,String accessToken,String projectedType) {

		ExceptionMessage exception = new ExceptionMessage();
		AlgorithmProcessType pType = AlgorithmProcessType.valueOf(StringUtils.toRootUpperCase(projectedType));
		logger.info("\n************* NEW STUDENT:***********************");
		GraduationStudentRecord gradResponse = gradStatusService.getGradStatus(studentID, accessToken,exception);
		if(exception.getExceptionName() != null) {
			AlgorithmResponse aR= new AlgorithmResponse();
			aR.setException(exception);
			return aR;
		}
		logger.info("**** Fetched Student Information: ****");
		if(gradResponse != null && !gradResponse.getStudentStatus().equals("MER")) {
			ProcessorData data = new ProcessorData(gradResponse,null,accessToken,studentID,batchId,exception);
	     	AlgorithmProcess process = algorithmProcessFactory.createProcess(pType);
	     	data = process.fire(data);
	        return data.getAlgorithmResponse();		     	
		}else {
			AlgorithmResponse aR= new AlgorithmResponse();
			ExceptionMessage exp = new ExceptionMessage();
			exp.setExceptionName("STUDENT-NOT-ACCEPTABLE");
			exp.setExceptionDetails(String.format("Graduation Algorithm Cannot be Run for this Student because of status %s",gradResponse != null ?gradResponse.getStudentStatus():"UNKNOWN"));
			aR.setException(exp);
			return aR;
		}
	}

	public ReportData prepareReportData(String pen, String type, String accessToken) {
		type = Optional.ofNullable(type).orElse("");
		switch (type.toUpperCase()) {
			case "CERT":
				return reportService.prepareCertificateData(pen, accessToken, new ExceptionMessage());
			case "ACHV":
				ReportData reportData = new ReportData();
				reportData.getParameters().put("NOT SUPPORTED", "ACHV Report Data type not supported yet");
				return reportData;
			case "XML":
				return reportService.prepareTranscriptData(pen, true, accessToken, new ExceptionMessage());
			default:
				return reportService.prepareTranscriptData(pen, false, accessToken, new ExceptionMessage());
		}
	}

	public ReportData prepareReportData(GraduationData graduationData, String type, String accessToken) {
		type = Optional.ofNullable(type).orElse("");
		switch(type.toUpperCase()) {
			case "CERT":
				return reportService.prepareCertificateData(graduationData, accessToken, new ExceptionMessage());
			case "ACHV":
				ReportData reportData = new ReportData();
				reportData.getParameters().put("NOT SUPPORTED", "ACHV Report Data type not supported yet");
				return reportData;
			case "XML":
				return reportService.prepareTranscriptData(graduationData, true, accessToken, new ExceptionMessage());
			default:
				return reportService.prepareTranscriptData(graduationData, false, accessToken, new ExceptionMessage());
		}
	}

	public byte[] prepareTranscriptReport(String pen, String interim, String accessToken) {
		boolean isInterim = StringUtils.trimToNull(Optional.ofNullable(interim).orElse("")) != null;
		try {
			ReportData reportData = reportService.prepareTranscriptData(pen, isInterim, accessToken, new ExceptionMessage());

			ReportOptions options = new ReportOptions();
			options.setReportFile("transcript");
			options.setReportName("Transcript Report.pdf");
			ReportRequest reportParams = new ReportRequest();
			reportParams.setOptions(options);
			reportParams.setData(reportData);

			return webClient.post().uri(educGraduationApiConstants.getTranscriptReport())
					.headers(h -> {
						h.setBearerAuth(accessToken);
						h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
					}).body(BodyInserters.fromValue(reportParams)).retrieve().bodyToMono(byte[].class).block();
		}catch (Exception e) {
			return new byte[0];
		}

	}

	public Integer createAndStoreSchoolReports(List<String> uniqueSchoolList,String type,String accessToken) {
		int numberOfReports = 0;
		int counter = 1;
		try {
			ExceptionMessage exception = new ExceptionMessage();
			for(String usl:uniqueSchoolList) {
				counter++;
				if(counter%50 == 0) {
					ResponseObj obj = reportService.getTokenResponseObject();
					accessToken = obj.getAccess_token();
				}
				List<GraduationStudentRecord> stdList = gradStatusService.getStudentListByMinCode(usl,accessToken);
				SchoolTrax schoolDetails = schoolService.getSchoolDetails(usl, accessToken, exception);
				if (schoolDetails != null) {
					logger.info("*** School Details Acquired {}", schoolDetails.getSchoolName());
					if (stdList != null && !stdList.isEmpty()) {
						ca.bc.gov.educ.api.graduation.model.report.School schoolObj = new ca.bc.gov.educ.api.graduation.model.report.School();
						schoolObj.setMincode(schoolDetails.getMinCode());
						schoolObj.setName(schoolDetails.getSchoolName());
						List<Student> processedList = processStudentList(stdList,type);
						if(!processedList.isEmpty() && type.equalsIgnoreCase("TVRRUN")) {
							numberOfReports = processProjectedNonGradReport(schoolObj, processedList, usl, exception, accessToken, numberOfReports);
						}else {
							numberOfReports = processGradReport(schoolObj,processedList,usl,exception,accessToken,numberOfReports);
						}

					}
				}
			}
		}catch (Exception e) {
			logger.debug("Error {}",e.getLocalizedMessage());
		}
		return numberOfReports;
	}


	private int processGradReport(School schoolObj, List<Student> stdList, String mincode, ExceptionMessage exception, String accessToken, int numberOfReports) {
		ReportData gradReport = getReportDataObj(schoolObj,stdList);
		createAndSaveSchoolReportGradReport(gradReport, mincode, exception, accessToken,"GRADREG");
		numberOfReports++;
		return numberOfReports;
	}
	private ReportData getReportDataObj(School schoolObj, List<Student> stdList) {
		ReportData data = new ReportData();
		schoolObj.setStudents(stdList);
		data.setSchool(schoolObj);
		data.setOrgCode(StringUtils.startsWith(data.getSchool().getMincode(), "098") ? "YU" : "BC");
		data.setIssueDate(EducGraduationApiUtils.formatIssueDateForReportJasper(new java.sql.Date(System.currentTimeMillis()).toString()));
		return data;
	}
	private int processProjectedNonGradReport(School schoolObj, List<Student> stdList, String mincode, ExceptionMessage exception, String accessToken, int numberOfReports) {
		ReportData nongradProjected = getReportDataObj(schoolObj,stdList);
		createAndSaveSchoolReportNonGradReport(nongradProjected, mincode, exception, accessToken,"NONGRADPRJ");
		numberOfReports++;
		return numberOfReports;
	}
	private List<Student> processStudentList(List<GraduationStudentRecord> gradStudList, String type) {
		List<Student> stdPrjList = new ArrayList<>();
		for (GraduationStudentRecord gsr : gradStudList) {
			if(gsr.getStudentStatus().equals("CUR") && (gsr.getStudentGrade().equalsIgnoreCase("AD") || gsr.getStudentGrade().equalsIgnoreCase("12"))) {
				Student std = new Student();
				std.setFirstName(gsr.getLegalFirstName());
				std.setLastName(gsr.getLegalLastName());
				std.setMiddleName(gsr.getLegalMiddleNames());
				Pen pen = new Pen();
				pen.setPen(gsr.getPen());
				std.setPen(pen);
				std.setGrade(gsr.getStudentGrade());
				std.setGradProgram(gsr.getProgram());
				std.setLastUpdateDate(gsr.getUpdateDate());
				if(type.equalsIgnoreCase("REGALG")) {
					ca.bc.gov.educ.api.graduation.model.report.GraduationData gradData = new ca.bc.gov.educ.api.graduation.model.report.GraduationData();
					gradData.setGraduationDate(gsr.getProgramCompletionDate() != null ? EducGraduationApiUtils.parsingTraxDate(gsr.getProgramCompletionDate()):null);
					gradData.setHonorsFlag(gsr.getHonoursStanding() != null && gsr.getHonoursStanding().equalsIgnoreCase("Y"));
					std.setGraduationData(gradData);
					if(gsr.getProgramCompletionDate() != null)
						stdPrjList.add(std);
				}else {
					std.setGraduationData(new ca.bc.gov.educ.api.graduation.model.report.GraduationData());
					try {
						if (gsr.getStudentProjectedGradData() != null) {
							ProjectedRunClob projectedClob = new ObjectMapper().readValue(gsr.getStudentProjectedGradData(), ProjectedRunClob.class);
							std.setNonGradReasons(getNonGradReasons(projectedClob.getNonGradReasons()));
							if (!projectedClob.isGraduated())
								stdPrjList.add(std);
						}
					} catch (JsonProcessingException e) {
						logger.debug("JSON processing Error {}", e.getMessage());
					}
				}
			}

		}
		return stdPrjList;
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

	private void createAndSaveSchoolReportGradReport(ReportData data,String mincode,ExceptionMessage exception, String accessToken,String reportType) {
		ReportOptions options = new ReportOptions();
		options.setReportFile(String.format("%s_%s00_GRADREG",mincode, LocalDate.now().getYear()));
		options.setReportName(String.format("%s_%s00_GRADREG.pdf",mincode, LocalDate.now().getYear()));
		ReportRequest reportParams = new ReportRequest();
		reportParams.setOptions(options);
		reportParams.setData(data);

		String encodedPdf = null;
		try {
			byte[] bytesSAR = webClient.post().uri(educGraduationApiConstants.getSchoolgraduation())
					.headers(h -> {
						h.setBearerAuth(accessToken);
						h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
					}).body(BodyInserters.fromValue(reportParams)).retrieve().bodyToMono(byte[].class).block();
			byte[] encoded = Base64.encodeBase64(bytesSAR);
			encodedPdf= new String(encoded, StandardCharsets.US_ASCII);
		}catch (Exception e) {
			exception.setExceptionName("GRAD_REPORT_API_DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
		}

		SchoolReports requestObj = new SchoolReports();
		requestObj.setReport(encodedPdf);
		requestObj.setSchoolOfRecord(mincode);
		requestObj.setReportTypeCode(reportType);

		try {
			webClient.post().uri(educGraduationApiConstants.getUpdateSchoolReport())
					.headers(h -> {
						h.setBearerAuth(accessToken);
						h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
					}).body(BodyInserters.fromValue(requestObj)).retrieve().bodyToMono(SchoolReports.class).block();
		}catch(Exception e) {
			if(exception.getExceptionName() == null) {
				exception.setExceptionName("GRAD_GRADUATION_REPORT_API_DOWN");
				exception.setExceptionDetails(e.getLocalizedMessage());
			}
		}
	}

	private void createAndSaveSchoolReportNonGradReport(ReportData data,String mincode,ExceptionMessage exception, String accessToken,String reportType) {
		data.setReportNumber("TRAX241B");
		data.setReportTitle("Grade 12 Examinations and TranscriptsGraduation Records and Achievement Data");
		data.setReportSubTitle("Projected Non-Grad Report for Students in Grade 12 and Adult Students");
		ReportOptions options = new ReportOptions();
		options.setReportFile(String.format("%s_%s00_NONGRADPRJ",mincode, LocalDate.now().getYear()));
		options.setReportName(String.format("%s_%s00_NONGRADPRJ.pdf",mincode, LocalDate.now().getYear()));
		ReportRequest reportParams = new ReportRequest();
		reportParams.setOptions(options);
		reportParams.setData(data);

		String encodedPdf = null;
		try {
			byte[] bytesSAR = webClient.post().uri(educGraduationApiConstants.getNonGradProjected())
					.headers(h -> {
						h.setBearerAuth(accessToken);
						h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
					}).body(BodyInserters.fromValue(reportParams)).retrieve().bodyToMono(byte[].class).block();
			byte[] encoded = Base64.encodeBase64(bytesSAR);
			encodedPdf= new String(encoded, StandardCharsets.US_ASCII);
		}catch (Exception e) {
			exception.setExceptionName("GRAD_REPORT_API_DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
		}

		SchoolReports requestObj = new SchoolReports();
		requestObj.setReport(encodedPdf);
		requestObj.setSchoolOfRecord(mincode);
		requestObj.setReportTypeCode(reportType);

		try {
			webClient.post().uri(educGraduationApiConstants.getUpdateSchoolReport())
					.headers(h -> {
						h.setBearerAuth(accessToken);
						h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
					}).body(BodyInserters.fromValue(requestObj)).retrieve().bodyToMono(SchoolReports.class).block();
		}catch(Exception e) {
			if(exception.getExceptionName() == null) {
				exception.setExceptionName("GRAD_GRADUATION_REPORT_API_DOWN");
				exception.setExceptionDetails(e.getLocalizedMessage());
			}
		}
	}
}