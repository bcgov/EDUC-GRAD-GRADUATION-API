package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.exception.ServiceException;
import ca.bc.gov.educ.api.graduation.model.dto.ExceptionMessage;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.graduation.model.dto.ProjectedRunClob;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.ThreadLocalStateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Service
public class GradStatusService {

	private static String studentAPIDown = "GRAD-STUDENT-API IS DOWN";
	@Autowired
    WebClient webClient;

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
    EducGraduationApiConstants educGraduationApiConstants;
	
	public GraduationStudentRecord getGradStatus(String studentID, String accessToken, ExceptionMessage exception) {
		try
		{
			return webClient.get().uri(String.format(educGraduationApiConstants.getReadGradStudentRecord(),studentID))
							.headers(h -> {
								h.setBearerAuth(accessToken);
								h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
							}).retrieve().bodyToMono(GraduationStudentRecord.class).block();
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
		obj.setStudentGradData(new ObjectMapper().writeValueAsString(graduationDataStatus));
	}
	
	public GraduationStudentRecord saveStudentGradStatus(String studentID,Long batchId,String accessToken, GraduationStudentRecord toBeSaved, ExceptionMessage exception) {
		try {
			String url = educGraduationApiConstants.getUpdateGradStatus();
			if(batchId != null) {
				url = url + "?batchId=%s";
			}
			return webClient.post().uri(String.format(url,studentID,batchId))
							.headers(h -> {
								h.setBearerAuth(accessToken);
								h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
							}).body(BodyInserters.fromValue(toBeSaved)).retrieve().bodyToMono(GraduationStudentRecord.class).block();
		}catch(Exception e) {
			exception.setExceptionName(studentAPIDown);
			exception.setExceptionDetails(e.getLocalizedMessage());
			GraduationStudentRecord rec = new GraduationStudentRecord();
			rec.setException(exception);
			return rec;
		}
	}

	public GraduationStudentRecord saveStudentRecordProjectedRun(ProjectedRunClob projectedRunClob, String studentID, Long batchId, String accessToken, ExceptionMessage exception) {
		try {
			String url = educGraduationApiConstants.getSaveStudentRecordProjectedRun();
			if(batchId != null) {
				url = url + "?batchId=%s";
			}
			return webClient.post().uri(String.format(url,studentID,batchId))
							.headers(h -> {
								h.setBearerAuth(accessToken);
								h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
							}).body(BodyInserters.fromValue(projectedRunClob)).retrieve().bodyToMono(GraduationStudentRecord.class).block();
		}catch(Exception e) {
			exception.setExceptionName(studentAPIDown);
			exception.setExceptionDetails(e.getLocalizedMessage());
			return null;
		}
	}

	@SneakyThrows
	public GraduationStudentRecord processProjectedResults(GraduationStudentRecord gradResponse, GraduationData graduationDataStatus)  {

		gradResponse.setStudentGradData(new ObjectMapper().writeValueAsString(graduationDataStatus));
		gradResponse.setProgramCompletionDate(graduationDataStatus.getGradStatus().getProgramCompletionDate());
		gradResponse.setGpa(graduationDataStatus.getGradStatus().getGpa());
		gradResponse.setHonoursStanding(graduationDataStatus.getGradStatus().getHonoursStanding());
		gradResponse.setRecalculateGradStatus(graduationDataStatus.getGradStatus().getRecalculateGradStatus());

		return gradResponse;
	}

	public void restoreStudentGradStatus(String studentID, String accessToken,boolean isGraduated) {		
		webClient.get().uri(String.format(educGraduationApiConstants.getUpdateGradStatusAlgoError(),studentID,isGraduated))
						.headers(h -> {
							h.setBearerAuth(accessToken);
							h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
						}).retrieve().bodyToMono(boolean.class).block();
	}

	public List<GraduationStudentRecord> getStudentListByMinCode(String schoolOfRecord, String accessToken) {
		final ParameterizedTypeReference<List<GraduationStudentRecord>> responseType = new ParameterizedTypeReference<>() {};
		List<GraduationStudentRecord> records;
		try {
			records = this.webClient.get()
					.uri(String.format(educGraduationApiConstants.getGradStudentListSchoolReport(),schoolOfRecord))
					.headers(h -> {
						h.setBearerAuth(accessToken);
					})
					.retrieve()
					.onStatus(HttpStatusCode::is5xxServerError,
							clientResponse -> Mono.error(new ServiceException("Server Error when accessing GRAD_STUDENT_API: ", clientResponse.statusCode().value())))
					.bodyToMono(responseType)
					.retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
					.filter(throwable -> throwable instanceof ServiceException)
							.onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
								throw new ServiceException("GRAD-STUDENT-API Service failed to process after max retries.", HttpStatus.SERVICE_UNAVAILABLE.value());
							}))
					.block();
		} catch (Exception e) {
			throw new ServiceException("GRAD-STUDENT-API Service failed to process: " + e.getLocalizedMessage(), HttpStatus.SERVICE_UNAVAILABLE.value(), e);
		}
		return records;
	}

}
