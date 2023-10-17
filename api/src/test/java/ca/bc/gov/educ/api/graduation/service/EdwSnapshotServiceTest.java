package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import ca.bc.gov.educ.api.graduation.util.JsonTransformer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class EdwSnapshotServiceTest {

    @Autowired
    EdwSnapshotService edwSnapshotService;

    @MockBean
    GradAlgorithmService gradAlgorithmService;

    @MockBean
    GradStatusService gradStatusService;

    @MockBean
    ReportService reportService;

    @MockBean
    RESTService restService;

    @Autowired
    JsonTransformer jsonTransformer;

    @Autowired
    GradValidation validation;

    @Autowired
    private ExceptionMessage exception;

    @Autowired
    private EducGraduationApiConstants constants;

    @MockBean
    WebClient webClient;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testProcessSnapshotForGradStudent() {
        EdwGraduationSnapshot snapshotRequest = new EdwGraduationSnapshot();
        snapshotRequest.setGradYear(Integer.valueOf("2023"));
        snapshotRequest.setPen("123456789");
        snapshotRequest.setStudentID(UUID.randomUUID());
        snapshotRequest.setGraduatedDate("202306");
        snapshotRequest.setSchoolOfRecord("12345678");

        var result = edwSnapshotService.processSnapshot(snapshotRequest, "123");
        assertNotNull(result);
        assertThat(result.getPen()).isEqualTo(snapshotRequest.getPen());
        assertThat(result.getGraduationFlag()).isEqualTo("Y");
    }

    @Test
    public void testProcessSnapshotForNonGradStudent() {
        EdwGraduationSnapshot snapshotRequest = new EdwGraduationSnapshot();
        snapshotRequest.setGradYear(Integer.valueOf("2023"));
        snapshotRequest.setPen("123456789");
        snapshotRequest.setStudentID(UUID.randomUUID());
        snapshotRequest.setGraduatedDate("");
        snapshotRequest.setSchoolOfRecord("12345678");

        GraduationStudentRecord gradResponse = new GraduationStudentRecord();
        gradResponse.setStudentID(snapshotRequest.getStudentID());
        gradResponse.setSchoolOfRecord(snapshotRequest.getSchoolOfRecord());
        gradResponse.setProgram("2023-EN");
        gradResponse.setStudentStatus("CUR");

        GraduationData graduationData = new GraduationData();
        graduationData.setGraduated(false);

        StudentNonGradReason nonGradReason = new StudentNonGradReason();
        nonGradReason.setPen(snapshotRequest.getPen());
        nonGradReason.setGraduationStudentRecordId(snapshotRequest.getStudentID());
        nonGradReason.setGradRule1("a");
        nonGradReason.setGradRule2("b");
        nonGradReason.setGradRule3("a");
        nonGradReason.setGradRule4("b");
        nonGradReason.setGradRule5("a");
        nonGradReason.setGradRule6("b");
        nonGradReason.setGradRule7("a");
        nonGradReason.setGradRule8("b");
        nonGradReason.setGradRule9("a");
        nonGradReason.setGradRule10("b");
        nonGradReason.setGradRule11("a");
        nonGradReason.setGradRule12("b");

        ProgramRequirementCode proReqCode1 = new ProgramRequirementCode();
        proReqCode1.setProReqCode("a");
        proReqCode1.setTraxReqChar("A");

        ProgramRequirementCode proReqCode2 = new ProgramRequirementCode();
        proReqCode2.setProReqCode("b");
        proReqCode2.setTraxReqChar("B");

        when(gradStatusService.getGradStatus(eq(snapshotRequest.getStudentID().toString()), eq("123"), any())).thenReturn(gradResponse);
        when(gradAlgorithmService.runHypotheticalGraduatedAlgorithm(snapshotRequest.getStudentID(), gradResponse.getProgram(), snapshotRequest.getGradYear().toString(), "123")).thenReturn(graduationData);

        when(restService.get(String.format(constants.getStudentNonGradReasonByPenUrl(), snapshotRequest.getPen()), StudentNonGradReason.class, "123")).thenReturn(nonGradReason);
        when(restService.get(constants.getProgramRequirementsEndpoint(), List.class, "123")).thenReturn(Arrays.asList(proReqCode1, proReqCode2));

        var result = edwSnapshotService.processSnapshot(snapshotRequest, "123");
        assertNotNull(result);
        assertThat(result.getPen()).isEqualTo(snapshotRequest.getPen());
        assertThat(result.getGraduationFlag()).isEqualTo("N");
        assertThat(result.getGraduationFlag()).isEqualTo("N");
        assertThat(result.getNonGradReason1()).isEqualTo("A");
    }

    @Test
    public void testProcessSnapshotForHypotheticalGradStudent() {
        EdwGraduationSnapshot snapshotRequest = new EdwGraduationSnapshot();
        snapshotRequest.setGradYear(Integer.valueOf("2023"));
        snapshotRequest.setPen("123456789");
        snapshotRequest.setStudentID(UUID.randomUUID());
        snapshotRequest.setGraduatedDate("");
        snapshotRequest.setSchoolOfRecord("12345678");

        GraduationStudentRecord gradResponse = new GraduationStudentRecord();
        gradResponse.setStudentID(snapshotRequest.getStudentID());
        gradResponse.setSchoolOfRecord(snapshotRequest.getSchoolOfRecord());
        gradResponse.setProgram("2023-EN");
        gradResponse.setStudentStatus("CUR");

        GradAlgorithmGraduationStudentRecord gradStatus = new GradAlgorithmGraduationStudentRecord();
        gradStatus.setStudentID(snapshotRequest.getStudentID());
        gradStatus.setProgram(gradResponse.getProgram());
        gradStatus.setGpa("3.80");
        gradStatus.setHonoursStanding("Y");

        GraduationData graduationData = new GraduationData();
        graduationData.setGraduated(true);
        graduationData.setGradStatus(gradStatus);

        when(gradStatusService.getGradStatus(eq(snapshotRequest.getStudentID().toString()), eq("123"), any())).thenReturn(gradResponse);
        when(gradAlgorithmService.runHypotheticalGraduatedAlgorithm(snapshotRequest.getStudentID(), gradResponse.getProgram(), snapshotRequest.getGradYear().toString(), "123")).thenReturn(graduationData);

        var result = edwSnapshotService.processSnapshot(snapshotRequest, "123");
        assertNotNull(result);
        assertThat(result.getPen()).isEqualTo(snapshotRequest.getPen());
        assertThat(result.getGraduationFlag()).isEqualTo("Y");
        assertThat(result.getGpa()).isEqualTo("3.80");
        assertThat(result.getHonoursStanding()).isEqualTo("Y");
    }


}
