package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.ExceptionMessage;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.graduation.model.dto.ProjectedRunClob;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.JsonTransformer;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class GradStatusService {

	private static final Logger logger = LoggerFactory.getLogger(GradStatusService.class);
	private static final String studentAPIDown = "GRAD-STUDENT-API IS DOWN";
    WebClient webClient;
	RESTService restService;
	RestTemplate restTemplate;
    EducGraduationApiConstants educGraduationApiConstants;
	JsonTransformer jsonTransformer;

	@Autowired
	public GradStatusService(WebClient webClient, RESTService restService, RestTemplate restTemplate, EducGraduationApiConstants educGraduationApiConstants, JsonTransformer jsonTransformer) {
		this.webClient = webClient;
		this.restService = restService;
		this.restTemplate = restTemplate;
		this.educGraduationApiConstants = educGraduationApiConstants;
		this.jsonTransformer = jsonTransformer;
	}

	public GraduationStudentRecord getGradStatus(String studentID, ExceptionMessage exception) {
		try
		{
			return restService.get(String.format(educGraduationApiConstants.getReadGradStudentRecord(),studentID), GraduationStudentRecord.class);
		} catch (Exception e) {
			exception.setExceptionName(studentAPIDown);
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
	}
	
	public GraduationStudentRecord prepareGraduationStatusObj(GraduationData graduationDataStatus) {
		GraduationStudentRecord obj = new GraduationStudentRecord();
		BeanUtils.copyProperties(graduationDataStatus.getGradStatus(), obj);
		prepareGraduationStatusData(obj, graduationDataStatus);
		return obj;
	}

	@SneakyThrows
	public void prepareGraduationStatusData(GraduationStudentRecord obj, GraduationData graduationDataStatus) {
		obj.setStudentGradData(jsonTransformer.marshall(graduationDataStatus));
	}
	
	public GraduationStudentRecord saveStudentGradStatus(String studentID, Long batchId, GraduationStudentRecord toBeSaved, ExceptionMessage exception) {
		try {
			String url = educGraduationApiConstants.getUpdateGradStatus();
			if(batchId != null) {
				url = url + "?batchId=%s";
			}
			return restService.post(String.format(url,studentID,batchId), toBeSaved, GraduationStudentRecord.class);
		} catch(Exception e) {
			exception.setExceptionName(studentAPIDown);
			exception.setExceptionDetails(e.getLocalizedMessage());
			GraduationStudentRecord rec = new GraduationStudentRecord();
			rec.setException(exception);
			return rec;
		}
	}

	public GraduationStudentRecord saveStudentRecordProjectedRun(ProjectedRunClob projectedRunClob, String studentID, Long batchId, ExceptionMessage exception) {
		try {
			String url = educGraduationApiConstants.getSaveStudentRecordProjectedRun();
			if(batchId != null) {
				url = url + "?batchId=%s";
			}
			return restService.post(String.format(url,studentID,batchId), projectedRunClob, GraduationStudentRecord.class);
		}catch(Exception e) {
			exception.setExceptionName(studentAPIDown);
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
	}

	@SneakyThrows
	public GraduationStudentRecord processProjectedResults(GraduationStudentRecord gradResponse, GraduationData graduationDataStatus)  {

		gradResponse.setStudentGradData(jsonTransformer.marshall(graduationDataStatus));
		gradResponse.setProgramCompletionDate(graduationDataStatus.getGradStatus().getProgramCompletionDate());
		gradResponse.setGpa(graduationDataStatus.getGradStatus().getGpa());
		gradResponse.setHonoursStanding(graduationDataStatus.getGradStatus().getHonoursStanding());
		gradResponse.setRecalculateGradStatus(graduationDataStatus.getGradStatus().getRecalculateGradStatus());

		return gradResponse;
	}

	public void restoreStudentGradStatus(String studentID, boolean isGraduated) {
		restService.get(String.format(educGraduationApiConstants.getUpdateGradStatusAlgoError(),studentID,isGraduated), Boolean.class);
	}

	public List<GraduationStudentRecord> getStudentListByMinCode(String schoolOfRecord) {
		var response = this.restService.get(String.format(educGraduationApiConstants.getGradStudentListSchoolReport(),schoolOfRecord), List.class);
		return jsonTransformer.convertValue(response, new TypeReference<>(){});
	}

}
