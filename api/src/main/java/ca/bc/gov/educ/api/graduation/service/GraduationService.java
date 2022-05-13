package ca.bc.gov.educ.api.graduation.service;


import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.model.report.ReportOptions;
import ca.bc.gov.educ.api.graduation.model.report.ReportRequest;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcess;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcessFactory;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcessType;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import ca.bc.gov.educ.api.graduation.util.ThreadLocalStateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

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
			exp.setExceptionDetails(String.format("Graduation Algorithm Cannot be Run for this Student because of status %s",gradResponse.getStudentStatus()));
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

	}
}