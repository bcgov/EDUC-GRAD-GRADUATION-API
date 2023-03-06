package ca.bc.gov.educ.api.graduation.util;

import ca.bc.gov.educ.api.graduation.model.dto.ProcessorData;
import ca.bc.gov.educ.api.graduation.model.dto.ResponseObj;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class TokenUtils {
    private static Logger logger = LoggerFactory.getLogger(TokenUtils.class);

    private final EducGraduationApiConstants constants;
    private final WebClient webClient;

    @Autowired
    public TokenUtils(final EducGraduationApiConstants constants, final WebClient webClient) {
        this.constants = constants;
        this.webClient = webClient;
    }

    public Pair<String, Long> checkAndGetAccessToken(Pair<String, Long> req) {
        long currentTime = System.currentTimeMillis();
        long startTime = req.getRight();
        long diff = (currentTime - startTime)/1000;

        logger.debug("=========> Check Duration: {} sec <===========", diff);
        if (diff > 120) { // if the previous step took more than 2 minutes, treat it as a long process, and get the new access token
            logger.debug("=========> Getting the new Access Token after 2 minutes <===========");
            ResponseObj responseObj = getTokenResponseObject();
            if (responseObj != null) {
                return Pair.of(responseObj.getAccess_token(), currentTime);
            }
        }
        return req;
    }

    public Pair<String, Long> getAccessToken(String accessToken) {
        long startTime = System.currentTimeMillis();
        ResponseObj responseObj = getTokenResponseObject();
        if (responseObj != null) {
            return Pair.of(responseObj.getAccess_token(), startTime);
        }
        return Pair.of(accessToken, startTime);
    }

    public void checkAndSetAccessToken(ProcessorData processorData) {
        long currentTime = System.currentTimeMillis();
        long diff = (currentTime - processorData.getStartTime())/1000;

        logger.debug("=========> Check Duration: {} sec <===========", diff);
        if (diff > 120) { // if the previous step took more than 2 minutes, treat it as a long process, and get the new access token
            logger.debug("=========> Getting the new Access Token after 2 minutes <===========");
            ResponseObj responseObj = getTokenResponseObject();
            if (responseObj != null) {
                processorData.setAccessToken(responseObj.getAccess_token());
                processorData.setStartTime(currentTime);
            }
        }
    }

    public void setAccessToken(ProcessorData processorData) {
        long startTime = System.currentTimeMillis();
        ResponseObj responseObj = getTokenResponseObject();
        if (responseObj != null) {
            processorData.setAccessToken(responseObj.getAccess_token());
            processorData.setStartTime(startTime);
        }
    }

    private ResponseObj getTokenResponseObject() {
        HttpHeaders httpHeadersKC = EducGraduationApiUtils.getHeaders(
                constants.getUserName(), constants.getPassword());
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        return this.webClient.post().uri(constants.getTokenUrl())
                .headers(h -> h.addAll(httpHeadersKC))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(map))
                .retrieve()
                .bodyToMono(ResponseObj.class).block();
    }
}
