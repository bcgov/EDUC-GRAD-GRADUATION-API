package ca.bc.gov.educ.api.graduation.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.LogHelper;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import ca.bc.gov.educ.api.graduation.util.GradValidation;

@Component
public class RequestInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	GradValidation validation;

	@Autowired
	EducGraduationApiConstants constants;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		validation.clear();
		return true;
	}

	/**
	 * After completion.
	 *
	 * @param request  the request
	 * @param response the response
	 * @param handler  the handler
	 * @param ex       the ex
	 */
	@Override
	public void afterCompletion(@NonNull final HttpServletRequest request, final HttpServletResponse response, @NonNull final Object handler, final Exception ex) {
		LogHelper.logServerHttpReqResponseDetails(request, response, constants.isSplunkLogHelperEnabled());
		val correlationID = request.getHeader(EducGraduationApiConstants.CORRELATION_ID);
		if (correlationID != null) {
			response.setHeader(EducGraduationApiConstants.CORRELATION_ID, request.getHeader(EducGraduationApiConstants.CORRELATION_ID));
		}
	}
}
