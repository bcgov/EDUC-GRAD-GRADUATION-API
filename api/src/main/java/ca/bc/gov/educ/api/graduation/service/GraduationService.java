package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.GradRequirement;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.School;
import ca.bc.gov.educ.api.graduation.model.report.*;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcess;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcessFactory;
import ca.bc.gov.educ.api.graduation.process.AlgorithmProcessType;
import ca.bc.gov.educ.api.graduation.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GraduationService {

    private static final Logger logger = LoggerFactory.getLogger(GraduationService.class);

    private static final String GRADREG = "GRADREG";
    private static final String NONGRADREG = "NONGRADREG";
    private static final String NONGRADPRJ = "NONGRADPRJ";
    private static final String REGALG = "REGALG";
    private static final String TVRRUN = "TVRRUN";

    @Autowired
    WebClient webClient;
    @Autowired
    AlgorithmProcessFactory algorithmProcessFactory;
    @Autowired
    GradStatusService gradStatusService;
    @Autowired
    SchoolService schoolService;
    @Autowired
    ReportService reportService;
    @Autowired
    TokenUtils tokenUtils;

    @Autowired
    EducGraduationApiConstants educGraduationApiConstants;

    @Autowired
    JsonTransformer jsonTransformer;

    public AlgorithmResponse graduateStudent(String studentID, Long batchId, String accessToken, String projectedType) {

        ExceptionMessage exception = new ExceptionMessage();
        AlgorithmProcessType pType = AlgorithmProcessType.valueOf(StringUtils.toRootUpperCase(projectedType));
        logger.info("\n************* NEW STUDENT:***********************");
        GraduationStudentRecord gradResponse = gradStatusService.getGradStatus(studentID, accessToken, exception);
        if (exception.getExceptionName() != null) {
            AlgorithmResponse aR = new AlgorithmResponse();
            aR.setException(exception);
            return aR;
        }
        logger.info("**** Fetched Student Information: ****");
        if (gradResponse != null && !gradResponse.getStudentStatus().equals("MER")) {
            ProcessorData data = new ProcessorData(gradResponse, null, accessToken, studentID, batchId, exception);
            AlgorithmProcess process = algorithmProcessFactory.createProcess(pType);
            data = process.fire(data);
            return data.getAlgorithmResponse();
        } else {
            AlgorithmResponse aR = new AlgorithmResponse();
            ExceptionMessage exp = new ExceptionMessage();
            exp.setExceptionName("STUDENT-NOT-ACCEPTABLE");
            exp.setExceptionDetails(String.format("Graduation Algorithm Cannot be Run for this Student because of status %s", gradResponse != null ? gradResponse.getStudentStatus() : "UNKNOWN"));
            aR.setException(exp);
            return aR;
        }
    }

    public ReportData prepareReportData(String pen, String type, String accessToken) {
        type = Optional.ofNullable(type).orElse("");
        switch (type.toUpperCase()) {
            case "CERT":
                return reportService.prepareCertificateData(pen, accessToken, new ExceptionMessage());
            case "ACHV":
                ReportData reportData = new ReportData();
                reportData.getParameters().put("NOT SUPPORTED", "ACHV Report Data type not supported yet");
                return reportData;
            case "XML":
                return reportService.prepareTranscriptData(pen, true, accessToken, new ExceptionMessage());
            default:
                return reportService.prepareTranscriptData(pen, false, accessToken, new ExceptionMessage());
        }
    }

    public ReportData prepareReportData(GraduationData graduationData, String type, String accessToken) {
        type = Optional.ofNullable(type).orElse("");
        switch (type.toUpperCase()) {
            case "CERT":
                return reportService.prepareCertificateData(graduationData, accessToken, new ExceptionMessage());
            case "ACHV":
                ReportData reportData = new ReportData();
                reportData.getParameters().put("NOT SUPPORTED", "ACHV Report Data type not supported yet");
                return reportData;
            case "XML":
                return reportService.prepareTranscriptData(graduationData, true, accessToken, new ExceptionMessage());
            default:
                return reportService.prepareTranscriptData(graduationData, false, accessToken, new ExceptionMessage());
        }
    }

    public byte[] prepareTranscriptReport(String pen, String interim, String accessToken) {

        boolean isInterim = StringUtils.trimToNull(Optional.ofNullable(interim).orElse("")) != null;
        ReportData reportData = reportService.prepareTranscriptData(pen, isInterim, accessToken, new ExceptionMessage());

        ReportOptions options = new ReportOptions();
        options.setReportFile("transcript");
        options.setReportName("Transcript Report.pdf");
        ReportRequest reportParams = new ReportRequest();
        reportParams.setOptions(options);
        reportParams.setData(reportData);

        return webClient.post().uri(educGraduationApiConstants.getTranscriptReport())
                .headers(h -> { h.setBearerAuth(accessToken); h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID()); }
                ).body(BodyInserters.fromValue(reportParams)).retrieve().bodyToMono(byte[].class).block();

    }

    public byte[] getSchoolReports(List<String> uniqueSchoolList, String type, String accessToken) {
        byte[] result = new byte[0];
        Pair<String, Long> res = Pair.of(accessToken, System.currentTimeMillis());
        int i = 0;
        for (String usl : uniqueSchoolList) {
            if (i == 0) {
                res = getAccessToken(accessToken);
            } else {
                res = checkAndGetAccessToken(res);
            }
            accessToken = res.getLeft();

            List<GraduationStudentRecord> stdList = gradStatusService.getStudentListByMinCode(usl, accessToken);
            SchoolTrax schoolDetails = schoolService.getSchoolDetails(usl, accessToken, new ExceptionMessage());
            if (schoolDetails != null) {
                ca.bc.gov.educ.api.graduation.model.report.School schoolObj = new ca.bc.gov.educ.api.graduation.model.report.School();
                schoolObj.setMincode(schoolDetails.getMinCode());
                schoolObj.setName(schoolDetails.getSchoolName());
                ReportData gradReport;
                switch (type) {
                    case GRADREG:
                        List<Student> gradRegStudents = processStudentList(filterStudentList(stdList, GRADREG), REGALG);
                        gradReport = getReportDataObj(schoolObj, gradRegStudents);
                        return getSchoolReportGradRegReport(gradReport, schoolObj.getMincode(), accessToken);
                    case NONGRADREG:
                        List<Student> nonGradRegStudents = processStudentList(filterStudentList(stdList, NONGRADREG), REGALG);
                        gradReport = getReportDataObj(schoolObj, nonGradRegStudents);
                        return getSchoolReportNonGradRegReport(gradReport, schoolObj.getMincode(), accessToken);
                    case NONGRADPRJ:
                        List<Student> nonGradPrjStudents = processStudentList(filterStudentList(stdList, NONGRADPRJ), TVRRUN);
                        gradReport = getReportDataObj(schoolObj, nonGradPrjStudents);
                        return getSchoolReportNonGradPrjReport(gradReport, schoolObj.getMincode(), accessToken);
                    default:
                        return result;
                }
            }
            i++;
        }
        return result;
    }

    public Integer createAndStoreSchoolReports(List<String> uniqueSchoolList, String type, String accessToken) {
        int numberOfReports = 0;
        Pair<String, Long> res = Pair.of(accessToken, System.currentTimeMillis());
        ExceptionMessage exception = new ExceptionMessage();
        int i = 0;
        for (String usl : uniqueSchoolList) {
            if (i == 0) {
                res = getAccessToken(accessToken);
            } else {
                res = checkAndGetAccessToken(res);
            }
            accessToken = res.getLeft();

            List<GraduationStudentRecord> stdList = gradStatusService.getStudentListByMinCode(usl, accessToken);
            SchoolTrax schoolDetails = schoolService.getSchoolDetails(usl, accessToken, exception);
            if (schoolDetails != null) {
                logger.info("*** School Details Acquired {}", schoolDetails.getSchoolName());
                if (stdList != null && !stdList.isEmpty()) {
                    ca.bc.gov.educ.api.graduation.model.report.School schoolObj = new ca.bc.gov.educ.api.graduation.model.report.School();
                    schoolObj.setMincode(schoolDetails.getMinCode());
                    schoolObj.setName(schoolDetails.getSchoolName());
                    if (TVRRUN.equalsIgnoreCase(type)) {
                        List<Student> nonGradPrjStudents = processStudentList(filterStudentList(stdList, NONGRADPRJ), type);
                        if (!nonGradPrjStudents.isEmpty()) {
                            logger.info("*** Process processNonGradPrjReport {} for {} students", schoolObj.getMincode(), nonGradPrjStudents.size());
                            numberOfReports = processNonGradPrjReport(schoolObj, nonGradPrjStudents, usl, accessToken, numberOfReports);
                        }
                    } else {
                        List<Student> gradRegStudents = processStudentList(filterStudentList(stdList, GRADREG), type);
                        if (!gradRegStudents.isEmpty()) {
                            logger.info("*** Process processGradRegReport {} for {} students", schoolObj.getMincode(), gradRegStudents.size());
                            numberOfReports = processGradRegReport(schoolObj, gradRegStudents, usl, accessToken, numberOfReports);
                        }
                        res = checkAndGetAccessToken(res);
                        accessToken = res.getLeft();
                        List<Student> nonGradRegStudents = processStudentList(filterStudentList(stdList, NONGRADREG), type);
                        if (!nonGradRegStudents.isEmpty()) {
                            logger.info("*** Process processNonGradRegReport {} for {} students", schoolObj.getMincode(), nonGradRegStudents.size());
                            numberOfReports = processNonGradRegReport(schoolObj, nonGradRegStudents, usl, accessToken, numberOfReports);
                        }
                    }
                }
            }
            i++;
        }
        return numberOfReports;
    }

    private List<GraduationStudentRecord> filterStudentList(List<GraduationStudentRecord> stdList, String type) {
        stdList.removeIf(p -> !"CUR".equalsIgnoreCase(p.getStudentStatus()));
        switch (type) {
            case GRADREG:
                return stdList.stream().filter(c -> (c.getProgramCompletionDate() != null && !"SCCP".equalsIgnoreCase(c.getProgram()))).collect(Collectors.toList());
            case NONGRADREG:
                return stdList.stream().filter(c -> c.getProgramCompletionDate() == null && ("AD".equalsIgnoreCase(c.getStudentGrade()) || "12".equalsIgnoreCase(c.getStudentGrade()))).collect(Collectors.toList());
            case NONGRADPRJ:
                return stdList.stream().filter(c -> ("AD".equalsIgnoreCase(c.getStudentGrade()) || "12".equalsIgnoreCase(c.getStudentGrade()))).collect(Collectors.toList());
            default:
                return stdList;
        }
    }

    private int processGradRegReport(School schoolObj, List<Student> stdList, String mincode, String accessToken, int numberOfReports) {
        ReportData gradReport = getReportDataObj(schoolObj, stdList);
        createAndSaveSchoolReportGradRegReport(gradReport, mincode, accessToken);
        numberOfReports++;
        return numberOfReports;
    }

    private int processNonGradRegReport(School schoolObj, List<Student> stdList, String mincode, String accessToken, int numberOfReports) {
        ReportData gradReport = getReportDataObj(schoolObj, stdList);
        createAndSaveSchoolReportNonGradRegReport(gradReport, mincode, accessToken);
        numberOfReports++;
        return numberOfReports;
    }

    private ReportData getReportDataObj(School schoolObj, List<Student> stdList) {
        ReportData data = new ReportData();
        schoolObj.setStudents(stdList);
        data.setSchool(schoolObj);
        data.setOrgCode(StringUtils.startsWith(data.getSchool().getMincode(), "098") ? "YU" : "BC");
        data.setIssueDate(EducGraduationApiUtils.formatIssueDateForReportJasper(new java.sql.Date(System.currentTimeMillis()).toString()));
        return data;
    }

    private int processNonGradPrjReport(School schoolObj, List<Student> stdList, String mincode, String accessToken, int numberOfReports) {
        ReportData nongradProjected = getReportDataObj(schoolObj, stdList);
        createAndSaveSchoolReportNonGradPrjReport(nongradProjected, mincode, accessToken);
        numberOfReports++;
        return numberOfReports;
    }

    @SneakyThrows
    private List<Student> processStudentList(List<GraduationStudentRecord> gradStudList, String type) {
        List<Student> stdPrjList = new ArrayList<>();
        for (GraduationStudentRecord gsr : gradStudList) {
            Student std = new Student();
            std.setFirstName(gsr.getLegalFirstName());
            std.setLastName(gsr.getLegalLastName());
            std.setMiddleName(gsr.getLegalMiddleNames());
            Pen pen = new Pen();
            pen.setPen(gsr.getPen());
            pen.setEntityID(gsr.getStudentID());
            std.setPen(pen);
            std.setGrade(gsr.getStudentGrade());
            std.setGradProgram(gsr.getProgram());
            std.setLastUpdateDate(gsr.getUpdateDate());
            if (type.equalsIgnoreCase(REGALG)) {
                ca.bc.gov.educ.api.graduation.model.report.GraduationData gradData = new ca.bc.gov.educ.api.graduation.model.report.GraduationData();
                gradData.setGraduationDate(gsr.getProgramCompletionDate() != null ? EducGraduationApiUtils.parsingTraxDate(gsr.getProgramCompletionDate()) : null);
                gradData.setHonorsFlag(gsr.getHonoursStanding() != null && gsr.getHonoursStanding().equalsIgnoreCase("Y"));
                std.setGraduationData(gradData);
                std.setNonGradReasons(getNonGradReasons(gsr.getNonGradReasons()));
                stdPrjList.add(std);
            } else {
                std.setGraduationData(new ca.bc.gov.educ.api.graduation.model.report.GraduationData());
                if (gsr.getStudentProjectedGradData() != null) {
                    ProjectedRunClob projectedClob = new ObjectMapper().readValue(gsr.getStudentProjectedGradData(), ProjectedRunClob.class);
                    std.setNonGradReasons(getNonGradReasons(projectedClob.getNonGradReasons()));
                    if (!projectedClob.isGraduated())
                        stdPrjList.add(std);
                }
            }
        }
        return stdPrjList;
    }

    private List<NonGradReason> getNonGradReasons(List<GradRequirement> nonGradReasons) {
        List<NonGradReason> nList = new ArrayList<>();
        if (nonGradReasons != null) {
            for (GradRequirement gR : nonGradReasons) {
                NonGradReason obj = new NonGradReason();
                obj.setCode(gR.getTranscriptRule());
                obj.setDescription(gR.getDescription());
                nList.add(obj);
            }
        }
        return nList;
    }

    private byte[] getSchoolReportGradRegReport(ReportData data, String mincode, String accessToken) {
        ReportOptions options = new ReportOptions();
        options.setReportFile(String.format("%s_%s00_GRADREG", mincode, LocalDate.now().getYear()));
        options.setReportName(String.format("%s_%s00_GRADREG.pdf", mincode, LocalDate.now().getYear()));
        ReportRequest reportParams = new ReportRequest();
        reportParams.setOptions(options);
        reportParams.setData(data);

        return webClient.post().uri(educGraduationApiConstants.getSchoolGraduation())
                .headers(h -> { h.setBearerAuth(accessToken); h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID()); }
                ).body(BodyInserters.fromValue(reportParams)).retrieve().bodyToMono(byte[].class).block();

    }

    private byte[] createAndSaveSchoolReportGradRegReport(ReportData data, String mincode, String accessToken) {

        byte[] bytesSAR = getSchoolReportGradRegReport(data, mincode, accessToken);

        String encodedPdf = getEncodedPdfFromBytes(bytesSAR);

        SchoolReports requestObj = getSchoolReports(mincode, encodedPdf, GRADREG);

        updateSchoolReport(accessToken, requestObj);

        return bytesSAR;
    }

    private void updateSchoolReport(String accessToken, SchoolReports requestObj) {
        webClient.post().uri(educGraduationApiConstants.getUpdateSchoolReport())
                .headers(h -> { h.setBearerAuth(accessToken); h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID()); }
                ).body(BodyInserters.fromValue(requestObj)).retrieve().bodyToMono(SchoolReports.class).block();
    }

    private String getEncodedPdfFromBytes(byte[] bytesSAR) {
        byte[] encoded = Base64.encodeBase64(bytesSAR);
        return new String(encoded, StandardCharsets.US_ASCII);
    }

    private byte[] getSchoolReportNonGradRegReport(ReportData data, String mincode, String accessToken) {
        ReportOptions options = new ReportOptions();
        options.setReportFile(String.format("%s_%s00_NONGRADREG", mincode, LocalDate.now().getYear()));
        options.setReportName(String.format("%s_%s00_NONGRADREG.pdf", mincode, LocalDate.now().getYear()));
        ReportRequest reportParams = new ReportRequest();
        reportParams.setOptions(options);
        reportParams.setData(data);

        return webClient.post().uri(educGraduationApiConstants.getSchoolNonGraduation())
                .headers(h -> { h.setBearerAuth(accessToken); h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID()); }
                ).body(BodyInserters.fromValue(reportParams)).retrieve().bodyToMono(byte[].class).block();

    }

    private void createAndSaveSchoolReportNonGradRegReport(ReportData data, String mincode, String accessToken) {

        byte[] bytesSAR = getSchoolReportNonGradRegReport(data, mincode, accessToken);

        String encodedPdf = getEncodedPdfFromBytes(bytesSAR);

        SchoolReports requestObj = getSchoolReports(mincode, encodedPdf, NONGRADREG);

        updateSchoolReport(accessToken, requestObj);

    }

    private byte[] getSchoolReportNonGradPrjReport(ReportData data, String mincode, String accessToken) {
        data.setReportTitle("Graduation Records and Achievement Data");
        data.setReportSubTitle("Projected Non-Grad Report for Students in Grade 12 and Adult Students");
        ReportOptions options = new ReportOptions();
        options.setReportFile(String.format("%s_%s00_NONGRADPRJ", mincode, LocalDate.now().getYear()));
        options.setReportName(String.format("%s_%s00_NONGRADPRJ.pdf", mincode, LocalDate.now().getYear()));
        ReportRequest reportParams = new ReportRequest();
        reportParams.setOptions(options);
        reportParams.setData(data);

        return webClient.post().uri(educGraduationApiConstants.getNonGradProjected())
                .headers(h -> { h.setBearerAuth(accessToken); h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID()); }
                ).body(BodyInserters.fromValue(reportParams)).retrieve().bodyToMono(byte[].class).block();
    }

    private void createAndSaveSchoolReportNonGradPrjReport(ReportData data, String mincode, String accessToken) {

        byte[] bytesSAR = getSchoolReportNonGradPrjReport(data, mincode, accessToken);

        String encodedPdf = getEncodedPdfFromBytes(bytesSAR);

        SchoolReports requestObj = getSchoolReports(mincode, encodedPdf, NONGRADPRJ);

        updateSchoolReport(accessToken, requestObj);

    }

    private SchoolReports getSchoolReports(String mincode, String encodedPdf, String nongradreg) {
        SchoolReports requestObj = new SchoolReports();
        requestObj.setReport(encodedPdf);
        requestObj.setSchoolOfRecord(mincode);
        requestObj.setReportTypeCode(nongradreg);
        return requestObj;
    }

    private Pair<String, Long> checkAndGetAccessToken(Pair<String, Long> req) {
        return tokenUtils.checkAndGetAccessToken(req);
    }

    private Pair<String, Long> getAccessToken(String accessToken) {
        return tokenUtils.getAccessToken(accessToken);
    }

}