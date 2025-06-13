package ca.bc.gov.educ.api.graduation.util;

import ca.bc.gov.educ.api.graduation.model.dto.ResponseObj;
import ca.bc.gov.educ.api.graduation.model.dto.ResponseObjCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class TokenUtils {

    private final ResponseObjCache responseObjCache;
    private final EducGraduationApiConstants constants;
    private final WebClient graduationApiClient;

    @Autowired
    public TokenUtils(final EducGraduationApiConstants constants, final @Qualifier("graduationApiClient") WebClient graduationApiClient, final ResponseObjCache responseObjCache) {
        this.constants = constants;
        this.graduationApiClient = graduationApiClient;
        this.responseObjCache = responseObjCache;
    }

    public Pair<String, Long> checkAndGetAccessToken(Pair<String, Long> req) {
        long currentTime = System.currentTimeMillis();
        long startTime = req.getRight();
        long diff = (currentTime - startTime)/1000;

        log.debug("=========> Check Duration: {} sec <===========", diff);
        if (diff > 120) { // if the previous step took more than 2 minutes, treat it as a long process, and get the new access token
            log.debug("=========> Getting the new Access Token after 2 minutes <===========");
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

    public String getAccessToken() {
        return this.getTokenResponseObject().getAccess_token();
    }

    public ResponseObj getTokenResponseObject() {
        if(responseObjCache.isExpired()){
            responseObjCache.setResponseObj(getResponseObj());
        }
        return responseObjCache.getResponseObj();
    }

    public ResponseObj getResponseObj() {
        log.debug("Fetch token");
        HttpHeaders httpHeadersKC = EducGraduationApiUtils.getHeaders(
                constants.getUserName(), constants.getPassword());
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        return this.graduationApiClient.post().uri(constants.getTokenUrl())
                .headers(h -> h.addAll(httpHeadersKC))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(map))
                .retrieve()
                .bodyToMono(ResponseObj.class).block();
    }
}
