package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.dto.institute.District;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    @Qualifier("graduationApiClient")
    WebClient graduationApiClient;

    @MockBean
    @Qualifier("gradEducStudentApiClient")
    WebClient gradEducStudentApiClient;

    @Autowired
    private EducGraduationApiConstants constants;

    @MockBean
    RESTService restService;

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
  @Autowired
  private DistrictService districtService;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetSchoolClob() {
        UUID schoolId = UUID.randomUUID();
        String mincode = "213123131";
        SchoolClob schtrax = new SchoolClob();
        schtrax.setSchoolId(schoolId.toString());
        schtrax.setMinCode(mincode);
        schtrax.setAddress1("1231");
        String url = String.format("https://educ-grad-trax-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v2/trax/school-clob/%s", schoolId);
        when(this.restService.get(url, SchoolClob.class, graduationApiClient)).thenReturn(schtrax);
        SchoolClob res = schoolService.getSchoolClob(schtrax.getSchoolId());

        assertNotNull(res);
        assertEquals(res.getSchoolId(),schoolId.toString());
        assertEquals(res.getMinCode(), mincode);
    }

    @Test
    public void testGetSchoolDetails() {
        UUID schoolId = UUID.randomUUID();
        String mincode = "213123131";
        ca.bc.gov.educ.api.graduation.model.dto.institute.School school = new ca.bc.gov.educ.api.graduation.model.dto.institute.School ();
        school.setSchoolId(schoolId.toString());
        school.setMincode(mincode);

        String url = String.format("https://educ-grad-trax-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v2/trax/school/%s", schoolId);
        when(this.restService.get(url, ca.bc.gov.educ.api.graduation.model.dto.institute.School.class, graduationApiClient))
                .thenReturn(school);
        ca.bc.gov.educ.api.graduation.model.dto.institute.School res = schoolService.getSchoolDetails(schoolId);

        assertNotNull(res);
        assertEquals(res.getSchoolId(),schoolId.toString());
        assertEquals(res.getMincode(), mincode);
    }

    @Test
    public void testGetSchoolDetailsNoToken() {
        UUID schoolId = UUID.randomUUID();
        String mincode = "213123131";
        SchoolClob schtrax = new SchoolClob();
        schtrax.setSchoolId(schoolId.toString());
        schtrax.setMinCode(mincode);
        schtrax.setAddress1("1231");
        mockTokenResponseObject();
        String url = String.format("https://educ-grad-trax-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v2/trax/school-clob/%s", schoolId);
        when(this.restService.get(url, SchoolClob.class, graduationApiClient)).thenReturn(schtrax);
        SchoolClob res = schoolService.getSchoolClob(schoolId);

        assertNotNull(res);
        assertEquals(res.getSchoolId(),schoolId.toString());
        assertEquals(res.getMinCode(),mincode);
    }

    @Test
    public void testGetDistrictDetailsNoToken() {
        UUID districtId = UUID.randomUUID();
        District schoolDistrict = new District();
        schoolDistrict.setDistrictId(districtId.toString());
        mockTokenResponseObject();
        String url = String.format("https://educ-grad-trax-api-77c02f-dev.apps.silver.devops.gov.bc.ca/api/v2/trax/district/%s", districtId);
        when(this.restService.get(url, District.class, graduationApiClient)).thenReturn(schoolDistrict);
        District res = districtService.getDistrictDetails(districtId);

        assertNotNull(res);
        assertEquals(res.getDistrictId(),districtId.toString());
    }

    private String mockTokenResponseObject() {
        final ResponseObj tokenObject = new ResponseObj();
        String mockToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJtbUhsTG4tUFlpdTl3MlVhRnh5Yk5nekQ3d2ZIb3ZBRFhHSzNROTk0cHZrIn0.eyJleHAiOjE2NjMxODg1MzMsImlhdCI6MTY2MzE4ODIzMywianRpIjoiZjA2ZWJmZDUtMzRlMi00NjY5LTg0MDktOThkNTc3OGZiYmM3IiwiaXNzIjoiaHR0cHM6Ly9zb2FtLWRldi5hcHBzLnNpbHZlci5kZXZvcHMuZ292LmJjLmNhL2F1dGgvcmVhbG1zL21hc3RlciIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiI4ZGFjNmM3Yy0xYjU5LTQ5ZDEtOTMwNC0wZGRkMTdlZGE0YWQiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJncmFkLWFkbWluLWNsaWVudCIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cHM6Ly9kZXYuZ3JhZC5nb3YuYmMuY2EiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6IldSSVRFX1NUVURFTlQgR1JBRF9CVVNJTkVTU19SIENSRUFURV9TVFVERU5UX1hNTF9UUkFOU0NSSVBUX1JFUE9SVCBDUkVBVEVfR1JBRF9BU1NFU1NNRU5UX1JFUVVJUkVNRU5UX0RBVEEgUkVBRF9TVFVERU5UIFJFQURfU0NIT09MIGVtYWlsIHByb2ZpbGUiLCJjbGllbnRJZCI6ImdyYWQtYWRtaW4tY2xpZW50IiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJjbGllbnRIb3N0IjoiMTQyLjMxLjQwLjE1NiIsInByZWZlcnJlZF91c2VybmFtZSI6InNlcnZpY2UtYWNjb3VudC1ncmFkLWFkbWluLWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxNDIuMzEuNDAuMTU2In0.AqSxYzfanjhxCEuxLVHcJWA528AglXezS0-6EBohLsAJ4W1prdcrcS7p6yv1mSBs9GEkCu7SZhjl97xWaNXf7Emd4O0ieawgfXhDdgCtWtpLc0X2NjRTcZmv9kCpr__LmX4Zl3temUShNLVsSI95iBD7GKQmx_qTMpf3fiXdmmBvpZIibEly9RBbrio5DirqdYKuj0CO3x7xruBdBQnutr_GK7_vkmpw-X4RAyxsCwxSDequot1cCgMcJvPb6SxOL0BHx01OjM84FPwf2DwDrLvhXXhh4KucykUJ7QfiA5unmlLQ0wfG-bBJDwpjlXazF8jOQNEcasABVTftW6s8NA";
        tokenObject.setAccess_token(mockToken);
        tokenObject.setRefresh_token("456");

        when(this.graduationApiClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(constants.getTokenUrl())).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(ResponseObj.class)).thenReturn(Mono.just(tokenObject));

        return mockToken;
    }

}

