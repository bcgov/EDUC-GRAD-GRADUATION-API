package ca.bc.gov.educ.api.graduation.service;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStatus;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.graduation.model.dto.ProcessorData;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcess;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcessFactory;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcessType;
import ca.bc.gov.educ.api.graduation.util.GradBusinessRuleException;
import ca.bc.gov.educ.api.graduation.util.GradValidation;


@Service
public class GraduationService {

	@SuppressWarnings("unused")
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
	SpecialProgramService specialProgramService;
	
	@Autowired
	ReportService reportService;
	
	@Autowired
	GradValidation validation;
	
	public AlgorithmResponse graduateStudent(String studentID, String accessToken,String projectedType) {
		try {
			AlgorithmProcessType pType = AlgorithmProcessType.valueOf(StringUtils.toRootUpperCase(projectedType));
			GraduationStudentRecord gradResponse = gradStatusService.getGradStatus(studentID, accessToken);
			logger.info("**** Fetched Student Information: ****");
			if(!gradResponse.getStudentStatus().equals("D") && !gradResponse.getStudentStatus().equals("M")) {
				ProcessorData data = new ProcessorData(gradResponse,null,accessToken,studentID);
		     	AlgorithmProcess process = algorithmProcessFactory.createProcess(pType);
		     	process.setInputData(data);
		     	data = process.fire();        
		        return data.getAlgorithmResponse();		     	
			}else {
				throw new GradBusinessRuleException(String.format("Graduation Algorithm Cannot be Run for this Student because of status %s",gradResponse.getStudentStatus()));
			}
		}catch(Exception e) {
			throw new GradBusinessRuleException(e.getMessage());
		}
	}
}