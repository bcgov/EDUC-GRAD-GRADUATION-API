package ca.bc.gov.educ.api.graduation.service;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.ExceptionMessage;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.graduation.model.dto.ProcessorData;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcess;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcessFactory;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcessType;
import ca.bc.gov.educ.api.graduation.util.GradValidation;


@Service
public class GraduationService {

	private static Logger logger = LoggerFactory.getLogger(GraduationService.class);

	@Autowired
    WebClient webClient;
	
	@Autowired
	private ExceptionMessage exception;
	
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
	
	public AlgorithmResponse graduateStudent(String studentID, String accessToken,String projectedType) {
		
		exception = new ExceptionMessage();
		AlgorithmProcessType pType = AlgorithmProcessType.valueOf(StringUtils.toRootUpperCase(projectedType));
		GraduationStudentRecord gradResponse = gradStatusService.getGradStatus(studentID, accessToken,exception);
		logger.info("**** Fetched Student Information: ****");
		if(exception.getExceptionName() != null) {
			AlgorithmResponse aR= new AlgorithmResponse();
			aR.setException(exception);
			return aR;
		}
		if(gradResponse != null && !gradResponse.getStudentStatus().equals("DEC") && !gradResponse.getStudentStatus().equals("MER")) {
			ProcessorData data = new ProcessorData(gradResponse,null,accessToken,studentID,exception);
	     	AlgorithmProcess process = algorithmProcessFactory.createProcess(pType);
	     	process.setInputData(data);
	     	data = process.fire();
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
}