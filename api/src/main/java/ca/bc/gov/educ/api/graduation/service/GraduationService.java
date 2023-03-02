package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.GradRequirement;
import ca.bc.gov.educ.api.graduation.model.dto.GraduationData;
import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.GraduationStatus;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GraduationService {

    private static final Logger logger = LoggerFactory.getLogger(GraduationService.class);

    private static final String GRADREG = "GRADREG";
    private static final String NONGRADREG = "NONGRADREG";
    private static final String NONGRADPRJ = "NONGRADPRJ";
    private static final String REGALG = "REGALG";
    private static final String TVRRUN = "TVRRUN";
    private static final String DISTREP_YE_SC = "DISTREP_YE_SC";
    private static final String DISTREP_YE_SD = "DISTREP_YE_SD";

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
        logger.debug("\n************* NEW STUDENT:***********************");
        GraduationStudentRecord gradResponse = gradStatusService.getGradStatus(studentID, accessToken, exception);
        if (exception.getExceptionName() != null) {
            AlgorithmResponse aR = new AlgorithmResponse();
            aR.setException(exception);
            return aR;
        }
        logger.debug("**** Fetched Student Information: ****");
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

    public byte[] prepareTranscriptReport(String pen, String interim, String preview, String accessToken) {

        boolean isInterim = StringUtils.trimToNull(Optional.ofNullable(interim).orElse("")) != null;
        boolean isPreview = StringUtils.trimToNull(Optional.ofNullable(preview).orElse("")) != null;
        ReportData reportData = reportService.prepareTranscriptData(pen, isInterim, accessToken, new ExceptionMessage());

        ReportOptions options = new ReportOptions();
        options.setReportFile("transcript");
        options.setReportName("Transcript Report.pdf");
        options.setPreview(isPreview);
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
                logger.debug("*** School Details Acquired {}", schoolDetails.getSchoolName());
                if (stdList != null && !stdList.isEmpty()) {
                    ca.bc.gov.educ.api.graduation.model.report.School schoolObj = new ca.bc.gov.educ.api.graduation.model.report.School();
                    schoolObj.setMincode(schoolDetails.getMinCode());
                    schoolObj.setName(schoolDetails.getSchoolName());
                    if (TVRRUN.equalsIgnoreCase(type)) {
                        List<Student> nonGradPrjStudents = processStudentList(filterStudentList(stdList, NONGRADPRJ), type);
                        logger.debug("*** Process processNonGradPrjReport {} for {} students", schoolObj.getMincode(), nonGradPrjStudents.size());
                        numberOfReports = processNonGradPrjReport(schoolObj, nonGradPrjStudents, usl, accessToken, numberOfReports);
                    } else {
                        List<Student> gradRegStudents = processStudentList(filterStudentList(stdList, GRADREG), type);
                        logger.debug("*** Process processGradRegReport {} for {} students", schoolObj.getMincode(), gradRegStudents.size());
                        numberOfReports = processGradRegReport(schoolObj, gradRegStudents, usl, accessToken, numberOfReports);
                        res = checkAndGetAccessToken(res);
                        accessToken = res.getLeft();
                        List<Student> nonGradRegStudents = processStudentList(filterStudentList(stdList, NONGRADREG), type);
                        logger.debug("*** Process processNonGradRegReport {} for {} students", schoolObj.getMincode(), nonGradRegStudents.size());
                        numberOfReports = processNonGradRegReport(schoolObj, nonGradRegStudents, usl, accessToken, numberOfReports);
                    }
                }
            }
            i++;
        }
        return numberOfReports;
    }

    @SneakyThrows
    public Integer createAndStoreDistrictYearEndReports(String accessToken) {
        Integer reportsCount = 0;
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport(accessToken);
        Map<School, List<School>> districtSchoolsMap = new HashMap<>();
        for(ReportGradStudentData reportGradStudentData: reportGradStudentDataList) {
            School district = populateDistrictObjectByReportGradStudentData(districtSchoolsMap, reportGradStudentData);
            processDistrictSchoolMap(districtSchoolsMap.get(district), reportGradStudentData);
        }
        for (var entry : districtSchoolsMap.entrySet()) {
            School district = entry.getKey();
            List<School> schools = entry.getValue();
            ReportRequest reportRequest = buildDistrictYearEndReportRequest(district);
            reportRequest.getData().getSchools().addAll(schools);
            accessToken = getAccessToken(accessToken).getLeft();
            byte[] reportAsBytes = getDistrictYearEndReportJasper(reportRequest, accessToken);
            saveDistrictSchoolYearEndReport(accessToken, reportRequest, DISTREP_YE_SD, reportAsBytes);
            reportsCount ++;
        }
        return reportsCount;
    }

    public Integer createAndStoreSchoolYearEndReports(String accessToken) {
        Integer reportsCount = 0;
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport(accessToken);
        Map<String, School> newCredentialsSchoolMap = new HashMap<>();
        for(ReportGradStudentData reportGradStudentData: reportGradStudentDataList) {
            School school = populateSchoolObjectByReportGradStudentData(newCredentialsSchoolMap, reportGradStudentData);
            Student student = processNewCredentialsSchoolMap(reportGradStudentData);
            if(student != null) {
                school.getStudents().add(student);
            }
        }
        Map<String, School> issuedTranscriptsSchoolMap = new HashMap<>();
        for(ReportGradStudentData reportGradStudentData: reportGradStudentDataList) {
            School school = populateSchoolObjectByReportGradStudentData(issuedTranscriptsSchoolMap, reportGradStudentData);
            Student student = processIssuedTranscriptsSchoolMap(reportGradStudentData);
            if(student != null) {
                school.getStudents().add(student);
            }
        }
        if(issuedTranscriptsSchoolMap.size() > newCredentialsSchoolMap.size()) {
            for (var entry : issuedTranscriptsSchoolMap.entrySet()) {
                School transcriptSchool = entry.getValue();
                ReportRequest reportRequest = buildSchoolYearEndReportRequest(transcriptSchool);
                ReportData reportData = new ReportData();
                reportData.setSchool(transcriptSchool);
                reportData.setOrgCode(getReportOrgCode(transcriptSchool.getMincode()));
                reportRequest.getDataMap().put("issuedTranscriptsReportData", reportData);
                School newCredentialsSchool = newCredentialsSchoolMap.get(transcriptSchool.getMincode());
                if(newCredentialsSchool != null) {
                    reportData = new ReportData();
                    reportData.setSchool(newCredentialsSchool);
                    reportData.setOrgCode(getReportOrgCode(newCredentialsSchool.getMincode()));
                    reportRequest.getDataMap().put("newCredentialsReportData", reportData);
                }
                accessToken = getAccessToken(accessToken).getLeft();
                byte[] reportAsBytes = getSchoolYearEndReportJasper(reportRequest, accessToken);
                saveDistrictSchoolYearEndReport(accessToken, reportRequest, DISTREP_YE_SC, reportAsBytes);
                reportsCount ++;
            }
        } else {
            for (var entry : newCredentialsSchoolMap.entrySet()) {
                School newCredentialsSchool = entry.getValue();
                ReportRequest reportRequest = buildSchoolYearEndReportRequest(newCredentialsSchool);
                ReportData reportData = new ReportData();
                reportData.setSchool(newCredentialsSchool);
                reportData.setOrgCode(getReportOrgCode(newCredentialsSchool.getMincode()));
                reportRequest.getDataMap().put("newCredentialsReportData", reportData);
                School transcriptSchool = issuedTranscriptsSchoolMap.get(newCredentialsSchool.getMincode());
                if(transcriptSchool != null) {
                    reportData = new ReportData();
                    reportData.setSchool(transcriptSchool);
                    reportData.setOrgCode(getReportOrgCode(transcriptSchool.getMincode()));
                    reportRequest.getDataMap().put("issuedTranscriptsReportData", reportData);
                }
                accessToken = getAccessToken(accessToken).getLeft();
                byte[] reportAsBytes = getSchoolYearEndReportJasper(reportRequest, accessToken);
                saveDistrictSchoolYearEndReport(accessToken, reportRequest, DISTREP_YE_SC, reportAsBytes);
                reportsCount ++;
            }
        }
        return reportsCount;
    }

    private void saveDistrictSchoolYearEndReport(String accessToken, ReportRequest reportRequest, String reportType, byte[] reportAsBytes) {
        String encodedPdf = getEncodedPdfFromBytes(reportAsBytes);
        SchoolReports schoolReports = getSchoolReports(reportRequest.getData().getSchool().getMincode(), encodedPdf, reportType);
        updateSchoolReport(accessToken, schoolReports);
    }

    private byte[] getSchoolYearEndReportJasper(ReportRequest reportRequest, String accessToken) {
        return webClient.post().uri(educGraduationApiConstants.getSchoolDistributionYearEnd())
                .headers(h -> { h.setBearerAuth(accessToken); h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID()); }
                ).body(BodyInserters.fromValue(reportRequest)).retrieve().bodyToMono(byte[].class).block();
    }

    private byte[] getDistrictYearEndReportJasper(ReportRequest reportRequest, String accessToken) {
        return webClient.post().uri(educGraduationApiConstants.getDistrictDistributionYearEnd())
                .headers(h -> { h.setBearerAuth(accessToken); h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID()); }
                ).body(BodyInserters.fromValue(reportRequest)).retrieve().bodyToMono(byte[].class).block();
    }

    private ReportRequest buildSchoolYearEndReportRequest(School school) {
        ReportRequest reportRequest = new ReportRequest();
        ReportOptions reportOptions = new ReportOptions();
        reportOptions.setReportName("schooldistributionyearend");
        reportOptions.setReportFile(String.format("%s School Distribution Report Year End.pdf", school.getMincode()));
        reportRequest.setOptions(reportOptions);
        ReportData headerData = new ReportData();
        School headerSchool = new School();
        headerSchool.setMincode(school.getMincode());
        headerSchool.setName(school.getName());
        headerData.setSchool(headerSchool);
        headerData.setOrgCode(getReportOrgCode(headerSchool.getMincode()));
        headerData.setIssueDate(new Date());
        reportRequest.setData(headerData);
        Map<String, ReportData> reportDataMap = new HashMap<>();
        reportRequest.setDataMap(reportDataMap);
        return reportRequest;
    }

    private ReportRequest buildDistrictYearEndReportRequest(School headerSchool) {
        ReportRequest reportRequest = new ReportRequest();
        ReportOptions reportOptions = new ReportOptions();
        reportOptions.setReportName("districtdistributionyearend");
        reportOptions.setReportFile(String.format("%s District Distribution Report Year End.pdf", headerSchool.getMincode()));
        reportRequest.setOptions(reportOptions);
        ReportData headerData = new ReportData();
        headerData.setSchool(headerSchool);
        headerData.setOrgCode(getReportOrgCode(headerSchool.getMincode()));
        headerData.setIssueDate(new Date());
        headerData.setSchools(new ArrayList<>());
        reportRequest.setData(headerData);
        return reportRequest;
    }

    private String getReportOrgCode(String mincode) {
        return (StringUtils.startsWith(mincode, "098") ? "YU" : "BC");
    }

    private School populateDistrictObjectByReportGradStudentData(Map<School, List<School>> districtSchoolsMap, ReportGradStudentData reportGradStudentData) {
        //district data, not school
        String distcode = StringUtils.substring(reportGradStudentData.getMincode(), 0, 3);
        boolean addNewDistrict = true;
        School district = null;
        for (var entry : districtSchoolsMap.entrySet()) {
            School currentDistrict = entry.getKey();
            if(StringUtils.equals(distcode, currentDistrict.getDistno())) {
                addNewDistrict = false;
                district = currentDistrict;
            }
        }
        if(addNewDistrict) {
            district = new School();
            district.setDistno(distcode);
            district.setMincode(distcode);
            district.setName(reportGradStudentData.getDistrictName());
            districtSchoolsMap.put(district, new ArrayList<>());
        }
        return district;
    }

    private School populateSchoolObjectByReportGradStudentData(Map<String, School> schoolMap, ReportGradStudentData reportGradStudentData) {
        String mincode = reportGradStudentData.getMincode();
        School school = schoolMap.get(mincode);
        if(school == null) {
            school = new School();
            school.setDistno(StringUtils.substring(mincode, 0, 3));
            school.setMincode(mincode);
            school.setName(reportGradStudentData.getSchoolName());
            school.setStudents(new ArrayList<>());
            schoolMap.put(mincode, school);
        }
        return school;
    }

    private void processDistrictSchoolMap(List<School> schools, ReportGradStudentData reportGradStudentData) {
        boolean addNewSchool = true;
        String distNo = StringUtils.substring(reportGradStudentData.getMincode(), 0, 3);
        for(School school: schools) {
            if(StringUtils.equals(school.getMincode(), reportGradStudentData.getMincode())) {
                addNewSchool = false;
                processDistrictSchool(school, reportGradStudentData);
            }
        }
        if(addNewSchool) {
            School school = new School();
            school.setDistno(distNo);
            school.setMincode(reportGradStudentData.getMincode());
            school.setName(reportGradStudentData.getSchoolName());
            schools.add(processDistrictSchool(school, reportGradStudentData));
        }
    }

    private School processDistrictSchool(School school, ReportGradStudentData reportGradStudentData) {
        if (reportGradStudentData.getCertificateTypes() != null && !reportGradStudentData.getCertificateTypes().isEmpty()) {
            for(GradCertificateType certType: reportGradStudentData.getCertificateTypes()) {
                switch (certType.getCode()) {
                    case "E", "EI", "O", "FN" -> school.getSchoolStatistic().setDogwoodCount(school.getSchoolStatistic().getDogwoodCount() + 1);
                    case "A", "AI" -> school.getSchoolStatistic().setAdultDogwoodCount(school.getSchoolStatistic().getAdultDogwoodCount() + 1);
                    case "F" -> school.getSchoolStatistic().setFrenchImmersionCount(school.getSchoolStatistic().getFrenchImmersionCount() + 1);
                    case "S" -> school.getSchoolStatistic().setProgramFrancophoneCount(school.getSchoolStatistic().getProgramFrancophoneCount() + 1);
                    case "SC", "SCF", "SCI" -> school.getSchoolStatistic().setEvergreenCount(school.getSchoolStatistic().getEvergreenCount() + 1);
                    default -> school.setTypeIndicator(null);
                }
            }
            school.getSchoolStatistic().setTotalCertificateCount(school.getSchoolStatistic().getTotalCertificateCount() + 1);
        }
        if(StringUtils.isNotBlank(reportGradStudentData.getTranscriptTypeCode())) {
            school.getSchoolStatistic().setTranscriptCount(school.getSchoolStatistic().getTranscriptCount() + 1);
        }
        return school;
    }

    private Student processNewCredentialsSchoolMap(ReportGradStudentData reportGradStudentData) {
        if(reportGradStudentData.getCertificateTypes() != null && !reportGradStudentData.getCertificateTypes().isEmpty()) {
            return populateStudentObjectByReportGradStudentData(reportGradStudentData);
        }
        return null;
    }

    private Student processIssuedTranscriptsSchoolMap(ReportGradStudentData reportGradStudentData) {
        if(StringUtils.isNotBlank(reportGradStudentData.getTranscriptTypeCode())) {
            return populateStudentObjectByReportGradStudentData(reportGradStudentData);
        }
        return null;
    }

    private Student populateStudentObjectByReportGradStudentData(ReportGradStudentData reportGradStudentData) {
        Student student = new Student();
        student.setPen(new Pen(reportGradStudentData.getPen(), reportGradStudentData.getGraduationStudentRecordId().toString()));
        student.setFirstName(reportGradStudentData.getFirstName());
        student.setMiddleName(reportGradStudentData.getMiddleName());
        student.setLastName(reportGradStudentData.getLastName());
        student.setGradProgram(reportGradStudentData.getProgramCode());

        GraduationStatus gradStatus = new GraduationStatus();
        gradStatus.setProgramCompletionDate(reportGradStudentData.getProgramCompletionDate());
        gradStatus.setSchoolAtGrad(reportGradStudentData.getMincode());
        gradStatus.setProgramName(reportGradStudentData.getProgramCode());
        student.setGraduationStatus(gradStatus);

        ca.bc.gov.educ.api.graduation.model.report.GraduationData gradData = new ca.bc.gov.educ.api.graduation.model.report.GraduationData();
        gradData.setGraduationDate(EducGraduationApiUtils.parseDate(reportGradStudentData.getProgramCompletionDate()));
        gradData.setProgramCodes(List.of(reportGradStudentData.getProgramCode()));
        gradData.setProgramNames(List.of(reportGradStudentData.getProgramName()));
        student.setGraduationData(gradData);

        return student;
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
            std.setCitizenship(gsr.getStudentCitizenship());
            Pen pen = new Pen();
            pen.setPen(gsr.getPen());
            pen.setEntityID(gsr.getStudentID());
            std.setPen(pen);
            std.setGrade(gsr.getStudentGrade());
            std.setGradProgram(gsr.getProgram());
            std.setLastUpdateDate(gsr.getUpdateDate());
            std.setGraduationStatus(GraduationStatus.builder()
                            .programCompletionDate(gsr.getProgramCompletionDate())
                            .honours(gsr.getHonoursStanding())
                            .gpa(gsr.getGpa())
                            .programName(gsr.getProgramName())
                            .studentStatus(gsr.getStudentStatus())
                            .studentStatusName(gsr.getStudentStatusName())
                            .studentGrade(gsr.getStudentGrade())
                            .schoolAtGrad(gsr.getSchoolAtGrad())
                            .schoolOfRecord(gsr.getSchoolOfRecord())
                    .build());
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