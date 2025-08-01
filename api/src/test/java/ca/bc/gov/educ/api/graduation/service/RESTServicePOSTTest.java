package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.exception.ServiceException;
import ca.bc.gov.educ.api.graduation.util.ThreadLocalStateUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
@ExtendWith(MockitoExtension.class)
public class RESTServicePOSTTest {

    @Autowired
    private RESTService restService;

    @MockBean
    private WebClient.RequestHeadersSpec requestHeadersMock;
    @MockBean
    private WebClient.RequestBodySpec requestBodyMock;
    @MockBean
    private WebClient.RequestBodyUriSpec requestBodyUriMock;
    @MockBean
    private WebClient.ResponseSpec responseMock;
    @MockBean(name = "graduationApiClient")
    @Qualifier("graduationApiClient")
    WebClient graduationApiClient;

    @MockBean
    ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;

    private static final byte[] TEST_BYTES = "The rain in Spain stays mainly on the plain.".getBytes();
    private static final String TEST_BODY = "{test:test}";
    private static final String TEST_URL = "https://fake.url.com";

    @Before
    public void setUp(){
        Mockito.reset(graduationApiClient, responseMock, requestHeadersMock, requestBodyMock, requestBodyUriMock);

        ThreadLocalStateUtil.clear();

        when(this.graduationApiClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(any(String.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(byte[].class)).thenReturn(Mono.just(TEST_BYTES));
    }

    @Test
    public void testPost_GivenProperData_Expect200Response(){
        ThreadLocalStateUtil.setCorrelationID("test-correlation-id");
        ThreadLocalStateUtil.setCurrentUser("test-user");
        when(this.responseMock.onStatus(any(), any())).thenReturn(this.responseMock);
        byte[] response = this.restService.post(TEST_URL, TEST_BODY, byte[].class, graduationApiClient);
        Assert.assertArrayEquals(TEST_BYTES, response);

    }

    @Test(expected = ServiceException.class)
    public void testPost_Given4xxErrorFromService_ExpectServiceError() {
        when(this.responseMock.onStatus(any(), any())).thenThrow(new ServiceException());
        this.restService.post(TEST_URL, TEST_BODY, byte[].class, graduationApiClient);
    }


    @Test(expected = ServiceException.class)
    public void testPost_Given5xxErrorFromService_ExpectWebClientRequestError(){
        when(requestBodyUriMock.uri(TEST_URL)).thenReturn(requestBodyMock);
        when(requestBodyMock.retrieve()).thenReturn(responseMock);

        Throwable cause = new RuntimeException("Simulated cause");
        when(responseMock.bodyToMono(byte[].class)).thenReturn(Mono.error(new WebClientRequestException(cause, HttpMethod.POST, null, new HttpHeaders())));
        this.restService.post(TEST_URL, TEST_BODY, byte[].class, graduationApiClient);
    }
}
