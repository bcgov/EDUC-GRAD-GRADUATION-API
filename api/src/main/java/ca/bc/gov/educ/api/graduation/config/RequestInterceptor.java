package ca.bc.gov.educ.api.graduation.config;

import ca.bc.gov.educ.api.graduation.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import java.time.Instant;

@Component
public class RequestInterceptor implements AsyncHandlerInterceptor {

	@Autowired
	GradValidation validation;

	EducGraduationApiConstants constants;

	LogHelper logHelper;

	@Autowired
	public RequestInterceptor(EducGraduationApiConstants constants, LogHelper logHelper) {
		this.constants = constants;
		this.logHelper = logHelper;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// for async this is called twice so need a check to avoid setting twice.
		if (request.getAttribute("startTime") == null) {
			final long startTime = Instant.now().toEpochMilli();
			request.setAttribute("startTime", startTime);
		}
		validation.clear();
		val correlationID = request.getHeader(EducGraduationApiConstants.CORRELATION_ID);
		if (correlationID != null) {
			ThreadLocalStateUtil.setCorrelationID(correlationID);
		}

		// username
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth instanceof JwtAuthenticationToken authenticationToken) {
			Jwt jwt = (Jwt) authenticationToken.getCredentials();
			String username = JwtUtil.getName(jwt, request);
			ThreadLocalStateUtil.setCurrentUser(username);
		}
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
		logHelper.logServerHttpReqResponseDetails(request, response, constants.isSplunkLogHelperEnabled());
		val correlationID = request.getHeader(EducGraduationApiConstants.CORRELATION_ID);
		if (correlationID != null) {
			response.setHeader(EducGraduationApiConstants.CORRELATION_ID, request.getHeader(EducGraduationApiConstants.CORRELATION_ID));
			ThreadLocalStateUtil.clear();
		}
	}
}
