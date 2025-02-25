package ca.bc.gov.educ.api.graduation.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public final class LogHelper {
  JsonTransformer jsonTransformer;

  private static final String EXCEPTION = "Exception ";

  @Autowired
  public LogHelper(JsonTransformer jsonTransformer) {
    this.jsonTransformer = jsonTransformer;
  }

  public void logServerHttpReqResponseDetails(@NonNull final HttpServletRequest request, final HttpServletResponse response, final boolean logging) {
    if (!logging) return;
    try {
      final int status = response.getStatus();
      val totalTime = Instant.now().toEpochMilli() - (Long) request.getAttribute("startTime");
      final Map<String, Object> httpMap = new HashMap<>();
      httpMap.put("server_http_response_code", status);
      httpMap.put("server_http_request_method", request.getMethod());
      httpMap.put("server_http_query_params", request.getQueryString());
      val correlationID = request.getHeader(EducGraduationApiConstants.CORRELATION_ID);
      if (correlationID != null) {
        httpMap.put("correlation_id", correlationID);
      }
      val headerUserName = request.getHeader(EducGraduationApiConstants.HEADER_USER_NAME);
      if (headerUserName != null) {
        httpMap.put("header_user_name", headerUserName);
      }
      val requestSource = request.getHeader(EducGraduationApiConstants.REQUEST_SOURCE);
      if (requestSource != null) {
        httpMap.put("request_source", requestSource);
      }
      httpMap.put("server_http_request_url", String.valueOf(request.getRequestURL()));
      httpMap.put("server_http_request_processing_time_ms", totalTime);
      httpMap.put("server_http_request_payload", String.valueOf(request.getAttribute("payload")));
      httpMap.put("server_http_request_remote_address", request.getRemoteAddr());
      MDC.putCloseable("httpEvent", jsonTransformer.marshall(httpMap));
      log.info("");
      MDC.clear();
    } catch (final Exception exception) {
      log.error(EXCEPTION, exception);
    }
  }

  public void logClientHttpReqResponseDetails(@NonNull final HttpMethod method, final String url, final int responseCode, final List<String> correlationID,
                                              final List<String> headerUserName, final List<String> requestSource, final boolean logging) {
    if (!logging) return;
    try {
      final Map<String, Object> httpMap = new HashMap<>();
      httpMap.put("client_http_response_code", responseCode);
      httpMap.put("client_http_request_method", method.toString());
      httpMap.put("client_http_request_url", url);
      if (correlationID != null) {
        httpMap.put("correlation_id", String.join(",", correlationID));
      }
      if (headerUserName != null) {
        httpMap.put("header_user_name", String.join(",", headerUserName));
      }
      if (correlationID != null) {
        httpMap.put("request_source", String.join(",", requestSource));
      }
      MDC.putCloseable("httpEvent", jsonTransformer.marshall(httpMap));
      log.info("");
      MDC.clear();
    } catch (final Exception exception) {
      log.error(EXCEPTION, exception);
    }
  }
}
