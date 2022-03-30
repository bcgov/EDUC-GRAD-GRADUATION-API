package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.process.GraduateStudentProcess;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.graduation.model.dto.ExceptionMessage;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;

@Service
public class GradStatusService {

	private static Logger logger = LoggerFactory.getLogger(GradStatusService.class);

	@Autowired
    WebClient webClient;

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
    EducGraduationApiConstants educGraduationApiConstants;
	
	public GraduationStudentRecord getGradStatus(String studentID, String accessToken, ExceptionMessage exception) {
		try
		{
			return webClient.get().uri(String.format(educGraduationApiConstants.getReadGradStudentRecord(),studentID)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GraduationStudentRecord.class).block();
		} catch (Exception e) {
			exception.setExceptionName("GRAD-STUDENT-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
	}
	
	public GraduationStudentRecord prepareGraduationStatusObj(GraduationData graduationDataStatus) {
		GraduationStudentRecord obj = new GraduationStudentRecord();
		BeanUtils.copyProperties(graduationDataStatus.getGradStatus(), obj);
		try {
			obj.setStudentGradData(new ObjectMapper().writeValueAsString(graduationDataStatus));
		} catch (JsonProcessingException e) {
			e.getMessage();
		}
		return obj;
	}
	
	public GraduationStudentRecord saveStudentGradStatus(String studentID,Long batchId,String accessToken, GraduationStudentRecord toBeSaved, ExceptionMessage exception) {
		try {
			String url = educGraduationApiConstants.getUpdateGradStatus();
			if(batchId != null) {
				url = url + "?batchId=%s";
			}
			return webClient.post().uri(String.format(url,studentID,batchId)).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(toBeSaved)).retrieve().bodyToMono(GraduationStudentRecord.class).block();
		}catch(Exception e) {
			exception.setExceptionName("GRAD-STUDENT-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
	}

	public GraduationStudentRecord saveStudentRecordProjectedRun(String studentID,Long batchId,String accessToken, ExceptionMessage exception) {
		try {
			String url = educGraduationApiConstants.getSaveStudentRecordProjectedRun();
			if(batchId != null) {
				url = url + "?batchId=%s";
			}
			return webClient.post().uri(String.format(url,studentID,batchId)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GraduationStudentRecord.class).block();
		}catch(Exception e) {
			exception.setExceptionName("GRAD-STUDENT-API IS DOWN");
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
	}

	public GraduationStudentRecord processProjectedResults(GraduationStudentRecord gradResponse, GraduationData graduationDataStatus)  {

		try {
			gradResponse.setStudentGradData(new ObjectMapper().writeValueAsString(graduationDataStatus));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		gradResponse.setProgramCompletionDate(graduationDataStatus.getGradStatus().getProgramCompletionDate());
		gradResponse.setGpa(graduationDataStatus.getGradStatus().getGpa());
		gradResponse.setHonoursStanding(graduationDataStatus.getGradStatus().getHonoursStanding());
		gradResponse.setRecalculateGradStatus(graduationDataStatus.getGradStatus().getRecalculateGradStatus());

		return gradResponse;
	}

	public void restoreStudentGradStatus(String studentID, String accessToken,boolean isGraduated) {		
		webClient.get().uri(String.format(educGraduationApiConstants.getUpdateGradStatusAlgoError(),studentID,isGraduated)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(boolean.class).block();	
	}
}
