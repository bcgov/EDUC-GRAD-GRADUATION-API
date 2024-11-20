package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.School;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import ca.bc.gov.educ.api.graduation.util.JsonTransformer;
import ca.bc.gov.educ.api.graduation.util.TokenUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static ca.bc.gov.educ.api.graduation.service.SchoolReportsService.NONGRADDISTREP_SD;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class SchooReportsServiceTest {

	private static final String ADDRESS_LABEL_YE = "ADDRESS_LABEL_YE";
	private static final String DISTREP_YE_SD = "DISTREP_YE_SD";
	private static final String DISTREP_YE_SC = "DISTREP_YE_SC";
	private static final String ADDRESS_LABEL_SCHL = "ADDRESS_LABEL_SCHL";
	private static final String ADDRESS_LABEL_PSI = "ADDRESS_LABEL_PSI";
	private static final String DISTREP_SD = "DISTREP_SD";
	private static final String DISTREP_SC = "DISTREP_SC";

	@Autowired
	private SchoolReportsService schoolReportsService;

	@MockBean
	private ReportService reportService;

	@MockBean
	RESTService restService;

	@MockBean
	SchoolService schoolService;

	@MockBean
	private TokenUtils tokenUtils;

	@Autowired
	GradValidation validation;

	@MockBean
	WebClient webClient;

	@Autowired
	JsonTransformer jsonTransformer;

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
	private EducGraduationApiConstants constants;

	@SneakyThrows
	@Test
	public void testSchoolReports() {
		List<ReportGradStudentData> gradStudentDataList = createStudentSchoolData("json/studentSchoolYearEndResponse.json");
		Mockito.when(reportService.getStudentsForSchoolYearEndReport("accessToken")).thenReturn(gradStudentDataList);
		Mockito.when(reportService.getStudentsForSchoolNonGradYearEndReport("accessToken")).thenReturn(gradStudentDataList);
		Mockito.when(reportService.getStudentsForSchoolReport()).thenReturn(gradStudentDataList);

		byte[] bytesSAR1 = readBinaryFile("data/sample.pdf");

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(String.format(constants.getSchoolDistributionYearEnd()))).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(byte[].class)).thenReturn(Mono.just(bytesSAR1));

		byte[] bytesSAR2 = readBinaryFile("data/sample.pdf");

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(String.format(constants.getDistrictDistributionYearEnd()))).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(byte[].class)).thenReturn(Mono.just(bytesSAR2));

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(String.format(constants.getDistrictDistributionYearEndNonGrad()))).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(byte[].class)).thenReturn(Mono.just(bytesSAR2));

		byte[] bytesSAR3 = readBinaryFile("data/sample.pdf");

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(String.format(constants.getSchoolLabels()))).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(byte[].class)).thenReturn(Mono.just(bytesSAR3));

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(String.format(constants.getUpdateSchoolReport()))).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(SchoolReports.class)).thenReturn(Mono.just(new SchoolReports()));

		when(this.tokenUtils.getAccessToken(any())).thenReturn(Pair.of("accessToken", System.currentTimeMillis()));

		ca.bc.gov.educ.api.graduation.model.dto.School traxSchool = new ca.bc.gov.educ.api.graduation.model.dto.School();
		traxSchool.setMinCode("12345678");
		traxSchool.setAddress1("1231");

		when(this.restService.get(any(String.class), any(), any())).thenReturn(traxSchool);
		when(this.schoolService.getSchoolClob(any(String.class))).thenReturn(traxSchool);

		mockTokenResponseObject();

		Integer reportsCount = schoolReportsService.createAndStoreSchoolReports(DISTREP_YE_SC, "accessToken");
		assertTrue(reportsCount > 0);

		reportsCount = schoolReportsService.createAndStoreSchoolReports(DISTREP_SC, "accessToken");
		assertTrue(reportsCount > 0);

		District disttrax = new District();
		disttrax.setDistrictNumber("005");
		disttrax.setDisplayName("My District");

		when(this.schoolService.getSchoolClob(any(String.class))).thenReturn(traxSchool);
		when(this.schoolService.getDistrictDetails(disttrax.getDistrictNumber())).thenReturn(disttrax);

		reportsCount = schoolReportsService.createAndStoreDistrictReports(DISTREP_YE_SD, "accessToken");
		assertTrue(reportsCount > 0);

		reportsCount = schoolReportsService.createAndStoreDistrictReports(DISTREP_SD, "accessToken");
		assertTrue(reportsCount > 0);

		reportsCount = schoolReportsService.createAndStoreDistrictReports(NONGRADDISTREP_SD, "accessToken");
		assertTrue(reportsCount > 0);

		reportsCount = schoolReportsService.createAndStoreSchoolDistrictYearEndReports("accessToken", ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC);
		assertTrue(reportsCount > 0);

		School school = new School();
		school.setMincode("005994567");
		school.setName("Test School Name");

		List<String> schools = new ArrayList<>();
		schools.add(school.getMincode());
		schools.add("005");

		reportsCount = schoolReportsService.createAndStoreSchoolDistrictYearEndReports("accessToken", ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC, schools);
		assertTrue(reportsCount > 0);

		reportsCount = schoolReportsService.createAndStoreSchoolDistrictReports("accessToken", ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC);
		assertTrue(reportsCount > 0);

		reportsCount = schoolReportsService.createAndStoreSchoolDistrictReports("accessToken", gradStudentDataList, ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC);
		assertTrue(reportsCount > 0);

		reportsCount = schoolReportsService.createAndStoreSchoolLabelsReportsFromSchools(ADDRESS_LABEL_PSI, List.of(school), "accessToken", null);
		assertTrue(reportsCount > 0);

		byte[] result = schoolReportsService.getSchoolDistrictYearEndReports("accessToken", ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC);
		assertNotNull(result);

		result = schoolReportsService.getSchoolDistrictReports("accessToken", ADDRESS_LABEL_SCHL, DISTREP_SD, DISTREP_SC);
		assertNotNull(result);

		result = schoolReportsService.getSchoolYearEndReports("accessToken");
		assertNotNull(result);

		result = schoolReportsService.getSchoolReports("accessToken");
		assertNotNull(result);

		result = schoolReportsService.getDistrictYearEndReports("accessToken");
		assertNotNull(result);

		result = schoolReportsService.getDistrictYearEndNonGradReports("accessToken");
		assertNotNull(result);

		result = schoolReportsService.getDistrictReports("accessToken");
		assertNotNull(result);

		result = schoolReportsService.getSchoolLabelsReportsFromSchools("ADDRESS_LABEL_PSI", List.of(school), "accessToken");
		assertNotNull(result);

	}

	@SneakyThrows
	private List<ReportGradStudentData> createStudentSchoolData(String jsonPath) {
		String json = readFile(jsonPath);
		return (List<ReportGradStudentData>) jsonTransformer.unmarshall(json, new TypeReference<List<ReportGradStudentData>>(){});
	}

	private byte[] readBinaryFile(String path) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(path);
		return inputStream.readAllBytes();
	}

	private String readFile(String jsonPath) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(jsonPath);
		return readInputStream(inputStream);
	}

	private String readInputStream(InputStream is) throws Exception {
		StringBuffer sb = new StringBuffer();
		InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(streamReader);
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	private String mockTokenResponseObject() {
		final ResponseObj tokenObject = new ResponseObj();
		String mockToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJtbUhsTG4tUFlpdTl3MlVhRnh5Yk5nekQ3d2ZIb3ZBRFhHSzNROTk0cHZrIn0.eyJleHAiOjE2NjMxODg1MzMsImlhdCI6MTY2MzE4ODIzMywianRpIjoiZjA2ZWJmZDUtMzRlMi00NjY5LTg0MDktOThkNTc3OGZiYmM3IiwiaXNzIjoiaHR0cHM6Ly9zb2FtLWRldi5hcHBzLnNpbHZlci5kZXZvcHMuZ292LmJjLmNhL2F1dGgvcmVhbG1zL21hc3RlciIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiI4ZGFjNmM3Yy0xYjU5LTQ5ZDEtOTMwNC0wZGRkMTdlZGE0YWQiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJncmFkLWFkbWluLWNsaWVudCIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cHM6Ly9kZXYuZ3JhZC5nb3YuYmMuY2EiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6IldSSVRFX1NUVURFTlQgR1JBRF9CVVNJTkVTU19SIENSRUFURV9TVFVERU5UX1hNTF9UUkFOU0NSSVBUX1JFUE9SVCBDUkVBVEVfR1JBRF9BU1NFU1NNRU5UX1JFUVVJUkVNRU5UX0RBVEEgUkVBRF9TVFVERU5UIFJFQURfU0NIT09MIGVtYWlsIHByb2ZpbGUiLCJjbGllbnRJZCI6ImdyYWQtYWRtaW4tY2xpZW50IiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJjbGllbnRIb3N0IjoiMTQyLjMxLjQwLjE1NiIsInByZWZlcnJlZF91c2VybmFtZSI6InNlcnZpY2UtYWNjb3VudC1ncmFkLWFkbWluLWNsaWVudCIsImNsaWVudEFkZHJlc3MiOiIxNDIuMzEuNDAuMTU2In0.AqSxYzfanjhxCEuxLVHcJWA528AglXezS0-6EBohLsAJ4W1prdcrcS7p6yv1mSBs9GEkCu7SZhjl97xWaNXf7Emd4O0ieawgfXhDdgCtWtpLc0X2NjRTcZmv9kCpr__LmX4Zl3temUShNLVsSI95iBD7GKQmx_qTMpf3fiXdmmBvpZIibEly9RBbrio5DirqdYKuj0CO3x7xruBdBQnutr_GK7_vkmpw-X4RAyxsCwxSDequot1cCgMcJvPb6SxOL0BHx01OjM84FPwf2DwDrLvhXXhh4KucykUJ7QfiA5unmlLQ0wfG-bBJDwpjlXazF8jOQNEcasABVTftW6s8NA";
		tokenObject.setAccess_token(mockToken);
		tokenObject.setRefresh_token("456");

		when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.uri(constants.getTokenUrl())).thenReturn(this.requestBodyUriMock);
		when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.contentType(any())).thenReturn(this.requestBodyMock);
		when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
		when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
		when(this.responseMock.bodyToMono(ResponseObj.class)).thenReturn(Mono.just(tokenObject));

		return mockToken;
	}
}

