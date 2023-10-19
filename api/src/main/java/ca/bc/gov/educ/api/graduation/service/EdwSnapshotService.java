package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import ca.bc.gov.educ.api.graduation.util.JsonTransformer;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class EdwSnapshotService {

    final GradAlgorithmService gradAlgorithmService;

    final GradStatusService gradStatusService;

    final ReportService reportService;

    final EducGraduationApiConstants constants;

    final RESTService restService;

    final JsonTransformer jsonTransformer;

    @Autowired
    public EdwSnapshotService(EducGraduationApiConstants constants,
                              GradAlgorithmService gradAlgorithmService,
                              GradStatusService gradStatusService,
                              ReportService reportService,
                              RESTService restService,
                              JsonTransformer jsonTransformer) {
        this.constants = constants;
        this.gradAlgorithmService = gradAlgorithmService;
        this.gradStatusService = gradStatusService;
        this.reportService = reportService;
        this.restService = restService;
        this.jsonTransformer = jsonTransformer;
    }

    public EdwGraduationSnapshot processSnapshot(EdwGraduationSnapshot snapshotRequest, String accessToken) {
        Integer gradYear = snapshotRequest.getGradYear(); // yyyy
        String pen = snapshotRequest.getPen();
        String graduatedDate = snapshotRequest.getGraduatedDate(); // yyyyMM
        String schoolOfRecord = snapshotRequest.getSchoolOfRecord();

        EdwGraduationSnapshot snapshot;
        boolean isGraduated = StringUtils.isNotBlank(graduatedDate);
        if (isGraduated) {
            // retrieve honour_flag, gpa
            snapshot = populateSnapshot(gradYear, pen, graduatedDate, "Y", snapshotRequest.getHonoursStanding(), snapshotRequest.getGpa(), schoolOfRecord);
        } else {
            snapshot = runHypotheticalGradAlgorithm(pen, gradYear, schoolOfRecord, accessToken);
        }
        log.debug("Save EdwSnapshot for Student pen# {}", snapshotRequest.getPen());
        saveEdwSnapshotOfGraduationStatus(accessToken, snapshot);
        return snapshot;
    }

    private EdwGraduationSnapshot runHypotheticalGradAlgorithm(String pen, Integer gradYear, String schoolOfRecord, String accessToken) {
        UUID studentID = getStudentID(pen, accessToken);
        if (studentID == null) {
            return null;
        }

        EdwGraduationSnapshot snapshot;
        log.debug("Hypothetical Grad Algorithm run for Student ID: {}", studentID);
        GraduationStudentRecord gradResponse = gradStatusService.getGradStatus(studentID.toString(), accessToken, new ExceptionMessage());
        String gradProgramCode = null;
        if (gradResponse != null && !gradResponse.getStudentStatus().equals("MER")) {
            gradProgramCode = gradResponse.getProgram();
        }
        boolean isHypotheticalPass = false;
        GraduationData graduationData = null;
        if (StringUtils.isNotBlank(gradProgramCode)) {
            // run hypothetical grad algorithm
            graduationData = gradAlgorithmService.runHypotheticalGraduatedAlgorithm(studentID, gradProgramCode, gradYear.toString(), accessToken);
            if (graduationData != null) {
                isHypotheticalPass = graduationData.isGraduated();
            }
        }
        if (isHypotheticalPass) {
            log.debug(" ==> Hypothetical Graduated!");
            String gpaStr = graduationData.getGradStatus().getGpa();
            BigDecimal gpa = NumberUtils.isCreatable(gpaStr)? new BigDecimal(gpaStr) : null;
            String honoursStanding = graduationData.getGradStatus().getHonoursStanding();
            snapshot = populateSnapshot(gradYear, pen, null, "Y", honoursStanding, gpa, schoolOfRecord);
        } else {
            // non-graduated student
            log.debug(" ==> Not Graduated!");
            List<ProgramRequirementCode> programRequirementCodes = getAllProgramRequirementCodeList(accessToken);
            StudentNonGradReason studentNonGradReason = getStudentNonGradReason(pen, accessToken);
            snapshot = populateSnapshot(gradYear, pen, null, "N", null, BigDecimal.ZERO, schoolOfRecord);
            if (studentNonGradReason != null) {
                setNonGradReasons(snapshot, studentNonGradReason, programRequirementCodes);
            }
        }
        return snapshot;
    }

    public StudentNonGradReason getStudentNonGradReason(String pen, String accessToken) {
        StudentNonGradReason response = null;
        try {
            response = this.restService.get(String.format(constants.getStudentNonGradReasonByPenUrl(), pen),
                    StudentNonGradReason.class,
                    accessToken);
        } catch (Exception e) {
            log.error("StudentNonGradReason is not found for pen # = {}", pen);
        }
        return response;
    }

    public void saveEdwSnapshotOfGraduationStatus(String accessToken, EdwGraduationSnapshot requestObj) {
        this.restService.post(constants.getEdwSnapshotOfGraduationStatus(),
                requestObj,
                EdwGraduationSnapshot.class,
                accessToken);
    }

    @SuppressWarnings("rawtypes")
    public List<ProgramRequirementCode> getAllProgramRequirementCodeList(String accessToken) {
        List response = this.restService.get(constants.getProgramRequirementsEndpoint(),
                List.class, accessToken);
        return jsonTransformer.convertValue(response, new TypeReference<>(){});
    }

    public UUID getStudentID(String pen, String accessToken) {
        GradSearchStudent penStudent = getStudentByPenFromStudentApi(pen, accessToken);
        if (penStudent != null) {
            return UUID.fromString(penStudent.getStudentID());
        } else {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    public GradSearchStudent getStudentByPenFromStudentApi(String pen, String accessToken) {
        List response = this.restService.get(String.format(constants.getPenStudentApiByPenUrl(), pen),
                List.class, accessToken);
        if (response != null && !response.isEmpty()) {
            List<GradSearchStudent> studentList = jsonTransformer.convertValue(response.get(0), new TypeReference<>(){});
            return studentList.get(0);
        }
        return null;
    }

    private EdwGraduationSnapshot populateSnapshot(Integer gradYear, String pen, String graduatedDate, String gradFlag, String honourFlag, BigDecimal gpa, String schoolOfRecord) {
        EdwGraduationSnapshot obj = new EdwGraduationSnapshot();
        obj.setGradYear(gradYear);
        obj.setPen(pen);
        obj.setGpa(gpa);
        obj.setHonoursStanding(honourFlag);
        obj.setGraduationFlag(gradFlag);
        obj.setGraduatedDate(graduatedDate);
        obj.setSchoolOfRecord(schoolOfRecord);
        return obj;
    }

    private void setNonGradReasons(EdwGraduationSnapshot snapshot, StudentNonGradReason studentNonGradReason, List<ProgramRequirementCode> programRequirementCodes) {
        // nonGradReason1
        if (StringUtils.isNotBlank(studentNonGradReason.getGradRule1())) {
            snapshot.setNonGradReason1(getTraxRequirementCode(studentNonGradReason.getGradRule1(), programRequirementCodes));
        }
        // nonGradReason2
        if (StringUtils.isNotBlank(studentNonGradReason.getGradRule2())) {
            snapshot.setNonGradReason2(getTraxRequirementCode(studentNonGradReason.getGradRule2(), programRequirementCodes));
        }
        // nonGradReason3
        if (StringUtils.isNotBlank(studentNonGradReason.getGradRule3())) {
            snapshot.setNonGradReason3(getTraxRequirementCode(studentNonGradReason.getGradRule3(), programRequirementCodes));
        }
        // nonGradReason4
        if (StringUtils.isNotBlank(studentNonGradReason.getGradRule4())) {
            snapshot.setNonGradReason4(getTraxRequirementCode(studentNonGradReason.getGradRule4(), programRequirementCodes));
        }
        // nonGradReason5
        if (StringUtils.isNotBlank(studentNonGradReason.getGradRule5())) {
            snapshot.setNonGradReason5(getTraxRequirementCode(studentNonGradReason.getGradRule5(), programRequirementCodes));
        }
        // nonGradReason6
        if (StringUtils.isNotBlank(studentNonGradReason.getGradRule6())) {
            snapshot.setNonGradReason6(getTraxRequirementCode(studentNonGradReason.getGradRule6(), programRequirementCodes));
        }
        // nonGradReason7
        if (StringUtils.isNotBlank(studentNonGradReason.getGradRule7())) {
            snapshot.setNonGradReason7(getTraxRequirementCode(studentNonGradReason.getGradRule7(), programRequirementCodes));
        }
        // nonGradReason8
        if (StringUtils.isNotBlank(studentNonGradReason.getGradRule8())) {
            snapshot.setNonGradReason8(getTraxRequirementCode(studentNonGradReason.getGradRule8(), programRequirementCodes));
        }
        // nonGradReason9
        if (StringUtils.isNotBlank(studentNonGradReason.getGradRule9())) {
            snapshot.setNonGradReason9(getTraxRequirementCode(studentNonGradReason.getGradRule9(), programRequirementCodes));
        }
        // nonGradReason10
        if (StringUtils.isNotBlank(studentNonGradReason.getGradRule10())) {
            snapshot.setNonGradReason10(getTraxRequirementCode(studentNonGradReason.getGradRule10(), programRequirementCodes));
        }
        // nonGradReason11
        if (StringUtils.isNotBlank(studentNonGradReason.getGradRule11())) {
            snapshot.setNonGradReason11(getTraxRequirementCode(studentNonGradReason.getGradRule11(), programRequirementCodes));
        }
        // nonGradReason12
        if (StringUtils.isNotBlank(studentNonGradReason.getGradRule12())) {
            snapshot.setNonGradReason12(getTraxRequirementCode(studentNonGradReason.getGradRule12(), programRequirementCodes));
        }
    }

    private String getTraxRequirementCode(String gradRule, List<ProgramRequirementCode> programRequirementCodes) {
        return programRequirementCodes.stream().filter(c -> gradRule.equalsIgnoreCase(c.getProReqCode())).findAny()
                .map(ProgramRequirementCode::getTraxReqChar).orElse(null);
    }
}
