package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Service
public class RESTService {

    private final WebClient webClient;

    @Autowired
    public RESTService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Generic GET call out to services. Uses blocking webclient and will throw
     * runtime exceptions. Will attempt retries if 5xx errors are encountered.
     * You can catch Exception in calling method.
     * @param url the url you are calling
     * @param clazz the return type you are expecting
     * @param accessToken access token
     * @return return type
     * @param <T> expected return type
     */
    public <T> T get(String url, Class<T> clazz, String accessToken) {
        T obj;
        try {
            obj = webClient
                    .get()
                    .uri(url)
                    .headers(h -> {
                        h.setBearerAuth(accessToken);
                    })
                    .retrieve()
                    // if 5xx errors, throw Service error
                    .onStatus(HttpStatusCode::is5xxServerError,
                            clientResponse -> Mono.error(new ServiceException(getErrorMessage(url, "5xx error."), clientResponse.statusCode().value())))
                    .bodyToMono(clazz)
                    // only does retry if initial error was 5xx as service may be temporarily down
                    // 4xx errors will always happen if 404, 401, 403 etc, so does not retry
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof ServiceException)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                throw new ServiceException(getErrorMessage(url, "Service failed to process after max retries."), HttpStatus.SERVICE_UNAVAILABLE.value());
                            }))
                    .block();
        } catch (Exception e) {
            // catches IOExceptions and the like
            throw new ServiceException(getErrorMessage(url, e.getLocalizedMessage()), HttpStatus.SERVICE_UNAVAILABLE.value(), e);
        }
        return obj;
    }

    public <T> List<T> get(String url, ParameterizedTypeReference<List<T>> responseType, String accessToken) {
        List<T> obj;
        try {
            obj = webClient
                    .get()
                    .uri(url)
                    .headers(h -> {
                        h.setBearerAuth(accessToken);
                    })
                    .retrieve()
                    // if 5xx errors, throw Service error
                    .onStatus(HttpStatusCode::is5xxServerError,
                            clientResponse -> Mono.error(new ServiceException(getErrorMessage(url, "5xx error."), clientResponse.statusCode().value())))
                    .bodyToMono(responseType)
                    // only does retry if initial error was 5xx as service may be temporarily down
                    // 4xx errors will always happen if 404, 401, 403 etc, so does not retry
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof ServiceException)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                throw new ServiceException(getErrorMessage(url, "Service failed to process after max retries."), HttpStatus.SERVICE_UNAVAILABLE.value());
                            }))
                    .block();
        } catch (Exception e) {
            // catches IOExceptions and the like
            throw new ServiceException(getErrorMessage(url, e.getLocalizedMessage()), HttpStatus.SERVICE_UNAVAILABLE.value(), e);
        }
        return obj;
    }

    private String getErrorMessage(String url, String errorMessage) {
        return "Service failed to process at url: " + url + " due to: " + errorMessage;
    }


}
