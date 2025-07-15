package ca.bc.gov.educ.api.graduation.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import ca.bc.gov.educ.api.graduation.util.ApiResponseMessage.MessageTypeEnum;
import ca.bc.gov.educ.api.graduation.util.ApiResponseModel;
import ca.bc.gov.educ.api.graduation.util.GradBusinessRuleException;
import ca.bc.gov.educ.api.graduation.util.GradValidation;

@Slf4j
@ControllerAdvice
public class RestErrorHandler extends ResponseEntityExceptionHandler {

	@Autowired
	GradValidation validation;

	@ExceptionHandler(value = { IllegalArgumentException.class, IllegalStateException.class })
	protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
		log.error("Illegal argument ERROR IS: " + ex.getClass().getName(), ex);
		ApiResponseModel<?> reponse = ApiResponseModel.ERROR(null, ex.getLocalizedMessage());
		validation.ifErrors(reponse::addErrorMessages);
		validation.ifWarnings(reponse::addWarningMessages);
		validation.clear();
		return new ResponseEntity<>(reponse, HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@ExceptionHandler(value = { JpaObjectRetrievalFailureException.class, DataRetrievalFailureException.class })
	protected ResponseEntity<Object> handleEntityNotFound(RuntimeException ex, WebRequest request) {
		log.error("JPA ERROR IS: " + ex.getClass().getName(), ex);
		validation.clear();
		return new ResponseEntity<>(ApiResponseModel.ERROR(null, ex.getLocalizedMessage()), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = { AccessDeniedException.class })
	protected ResponseEntity<Object> handleAuthorizationErrors(Exception ex, WebRequest request) {

		log.error("Authorization error EXCETPION IS: " + ex.getClass().getName());
		String message = "You are not authorized to access this resource.";
		validation.clear();
		return new ResponseEntity<>(ApiResponseModel.ERROR(null, message), HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(value = { GradBusinessRuleException.class })
	protected ResponseEntity<Object> handleIrisBusinessException(Exception ex, WebRequest request) {
		ApiResponseModel<?> response = ApiResponseModel.ERROR(null);
		validation.ifErrors(response::addErrorMessages);
		validation.ifWarnings(response::addWarningMessages);
		if (response.getMessages().isEmpty()) {
			response.addMessageItem(ex.getLocalizedMessage(), MessageTypeEnum.ERROR);
		}
		validation.clear();
		return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@ExceptionHandler(value = { OptimisticEntityLockException.class })
	protected ResponseEntity<Object> handleOptimisticEntityLockException(OptimisticEntityLockException ex, WebRequest request) {

		log.error("EXCEPTION IS: " + ex.getClass().getName(), ex);
		log.error("Illegal argument ERROR IS: " + ex.getClass().getName(), ex);
		ApiResponseModel<?> response = ApiResponseModel.ERROR(null);
		validation.ifErrors(response::addErrorMessages);
		validation.ifWarnings(response::addWarningMessages);
		if (!validation.hasErrors()) {
			response.addMessageItem(ex.getLocalizedMessage(), MessageTypeEnum.ERROR);
		}
		validation.clear();
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(value = { DataIntegrityViolationException.class })
	protected ResponseEntity<Object> handleSQLException(DataIntegrityViolationException ex, WebRequest request) {

		log.error("DATA INTEGRITY VIOLATION IS: " + ex.getClass().getName(), ex);
		String msg = ex.getLocalizedMessage();

		Throwable cause = ex.getCause();
		if (cause instanceof ConstraintViolationException) {
			ConstraintViolationException contraintViolation = (ConstraintViolationException) cause;
			if ("23000".equals(contraintViolation.getSQLState())) {
				// primary key violation - probably inserting a duplicate record
				msg = ex.getRootCause().getMessage();
			}
		}

		ApiResponseModel<?> reponse = ApiResponseModel.ERROR(null, msg);
		validation.ifErrors(reponse::addErrorMessages);
		validation.ifWarnings(reponse::addWarningMessages);
		validation.clear();
		return new ResponseEntity<>(reponse, HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@ExceptionHandler(value = { Exception.class })
	protected ResponseEntity<Object> handleUncaughtException(Exception ex, WebRequest request) {

		log.error("EXCEPTION IS: " + ex.getClass().getName(), ex);
		log.error("Illegal argument ERROR IS: " + ex.getClass().getName(), ex);
		ApiResponseModel<?> response = ApiResponseModel.ERROR(null);
		validation.ifErrors(response::addErrorMessages);
		validation.ifWarnings(response::addWarningMessages);
		if (!validation.hasErrors()) {
			response.addMessageItem(ex.getLocalizedMessage(), MessageTypeEnum.ERROR);
		}
		validation.clear();
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
}
