package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.graduation.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.graduation.model.dto.SchoolReports;
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
		Mockito.when(reportService.getStudentsForSchoolReport("accessToken")).thenReturn(gradStudentDataList);

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

		School school = new School();
		school.setMincode("005994567");
		school.setName("Test School Name");

		Integer reportsCount = schoolReportsService.createAndStoreSchoolReports(DISTREP_YE_SC, "accessToken");
		assertTrue(reportsCount > 0);

		reportsCount = schoolReportsService.createAndStoreDistrictReports(DISTREP_YE_SD, "accessToken");
		assertTrue(reportsCount > 0);

		reportsCount = schoolReportsService.createAndStoreSchoolDistrictYearEndReports("accessToken", ADDRESS_LABEL_YE, DISTREP_YE_SD, DISTREP_YE_SC);
		assertTrue(reportsCount > 0);

		List<String> schools = new ArrayList<>();
		schools.add(school.getMincode());

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
}
