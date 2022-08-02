package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class SchoolServiceTest {
	
	@Autowired
	private SchoolService schoolService;
	
	@Autowired
	GradValidation validation;
	
	@MockBean
    WebClient webClient;

    @Autowired
    private EducGraduationApiConstants constants;
    
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock
    private WebClient.RequestBodySpec requestBodyMock;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock
    private WebClient.ResponseSpec responseMock;
    @Mock
    private Mono<GraduationStudentRecord> monoResponse;
	
    @Before
    public void setUp() {
        openMocks(this);
    }    

	@After
    public void tearDown() {

    }

	@Test
	public void testGetSchoolDetails() {
		String mincode = "213123131";
		String accessToken = "accessToken";
		SchoolTrax schtrax = new SchoolTrax();
		schtrax.setMinCode(mincode);
		schtrax.setAddress1("1231");
		ExceptionMessage exception = new ExceptionMessage();
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolDetails(),mincode))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(SchoolTrax.class)).thenReturn(Mono.just(schtrax));

		SchoolTrax res = schoolService.getSchoolDetails(mincode, accessToken,exception);
		assertNotNull(res);
		assertEquals(res.getMinCode(),mincode);
	}

	@Test
	public void testGetSchoolDetails_throwsException() {
		String mincode = "1232131123";
		String accessToken = "accessToken";
		ExceptionMessage exception = new ExceptionMessage();
		when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
		when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolDetails(),mincode))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(Exception.class)).thenReturn(Mono.just(new Exception()));

		SchoolTrax res = schoolService.getSchoolDetails(mincode, accessToken,exception);
		assertNull(res);
	}
}
