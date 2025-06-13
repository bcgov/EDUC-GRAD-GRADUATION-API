package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.exception.ServiceException;
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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class GraduationService {

    private static final String GRADREG = "GRADREG";
    private static final String NONGRADREG = "NONGRADREG";
    private static final String NONGRADPRJ = "NONGRADPRJ";
    private static final String REGALG = "REGALG";
    private static final String TVRRUN = "TVRRUN";

    WebClient graduationApiClient;
    AlgorithmProcessFactory algorithmProcessFactory;
    GradStatusService gradStatusService;
    SchoolService schoolService;
    ReportService reportService;
    RESTService restService;
    EducGraduationApiConstants educGraduationApiConstants;
    SchoolYearDates schoolYearDates;
    JsonTransformer jsonTransformer;

    @Autowired
    public GraduationService(@Qualifier("graduationApiClient") WebClient graduationApiClient,
                             AlgorithmProcessFactory algorithmProcessFactory, GradStatusService gradStatusService,
                             SchoolService schoolService, ReportService reportService, RESTService restService,
                             EducGraduationApiConstants educGraduationApiConstants, SchoolYearDates schoolYearDates,
                             JsonTransformer jsonTransformer) {
        this.graduationApiClient = graduationApiClient;
        this.algorithmProcessFactory = algorithmProcessFactory;
        this.gradStatusService = gradStatusService;
        this.schoolService = schoolService;
        this.reportService = reportService;
        this.restService = restService;
        this.educGraduationApiConstants = educGraduationApiConstants;
        this.schoolYearDates = schoolYearDates;
        this.jsonTransformer = jsonTransformer;
    }

    public AlgorithmResponse graduateStudent(String studentID, Long batchId, String projectedType) {

        ExceptionMessage exception = new ExceptionMessage();
        AlgorithmProcessType pType = AlgorithmProcessType.valueOf(StringUtils.toRootUpperCase(projectedType));
        log.debug("\n************* NEW STUDENT:***********************");
        GraduationStudentRecord gradResponse = gradStatusService.getGradStatus(studentID, exception);
        if (exception.getExceptionName() != null) {
            AlgorithmResponse aR = new AlgorithmResponse();
            aR.setException(exception);
            return aR;
        }
        if (gradResponse != null && !gradResponse.getStudentStatus().equals("MER")) {
            log.debug("**** Fetched Student Information: {} ****", gradResponse.getStudentID());
            ProcessorData data = new ProcessorData(gradResponse, null, studentID, batchId, exception);
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

    public ReportData prepareReportData(String pen, String type) {
        type = Optional.ofNullable(type).orElse("");
        switch (type.toUpperCase()) {
            case "CERT":
                return reportService.prepareCertificateData(pen, new ExceptionMessage());
            case "ACHV":
                ReportData reportData = new ReportData();
                reportData.getParameters().put("NOT SUPPORTED", "ACHV Report Data type not supported yet");
                return reportData;
            case "XML":
                return reportService.prepareTranscriptData(pen, true, new ExceptionMessage());
            default:
                return reportService.prepareTranscriptData(pen, false, new ExceptionMessage());
        }
    }

    public ReportData prepareReportData(GraduationData graduationData, String type) {
        type = Optional.ofNullable(type).orElse("");
        switch (type.toUpperCase()) {
            case "CERT":
                return reportService.prepareCertificateData(graduationData, new ExceptionMessage());
            case "ACHV":
                ReportData reportData = new ReportData();
                reportData.getParameters().put("NOT SUPPORTED", "ACHV Report Data type not supported yet");
                return reportData;
            case "XML":
                return reportService.prepareTranscriptData(graduationData, true, new ExceptionMessage());
            default:
                return reportService.prepareTranscriptData(graduationData, false, new ExceptionMessage());
        }
    }

    public byte[] prepareTranscriptReport(String pen, String interim, String preview) {

        boolean isInterim = StringUtils.trimToNull(Optional.ofNullable(interim).orElse("")) != null;
        boolean isPreview = StringUtils.trimToNull(Optional.ofNullable(preview).orElse("")) != null;
        ReportData reportData = reportService.prepareTranscriptData(pen, isInterim, new ExceptionMessage());

        ReportOptions options = new ReportOptions();
        options.setReportFile("transcript");
        options.setReportName("Transcript Report.pdf");
        options.setPreview(isPreview);
        ReportRequest reportParams = new ReportRequest();
        reportParams.setOptions(options);
        reportParams.setData(reportData);

        try {
            return restService.post(educGraduationApiConstants.getTranscriptReport(), reportParams, byte[].class, graduationApiClient);
        } catch (ServiceException ex) {
            if(HttpStatus.NO_CONTENT.value() == ex.getStatusCode()) {
                return new byte[0];
            } else {
                throw ex;
            }
        }
    }

    /**
     * Regenerate Student Certificates
     *
     * @param pen
     * @param isOverwrite   true:  regenerate(delete and create) student certs
     *                      false: create or update student certs
     * @return the number of certificates created
     */
    public Integer createAndStoreStudentCertificates(String pen, boolean isOverwrite) {
        int i = 0;
        Pair<GraduationStudentRecord, GraduationData> pair = reportService.getGraduationStudentRecordAndGraduationData(pen);
        if (pair != null) {
            GraduationStudentRecord graduationStudentRecord = pair.getLeft();
            GraduationData graduationData = pair.getRight();

            if (isGraduated(graduationStudentRecord, graduationData)) {
                List<StudentOptionalProgram> projectedOptionalPrograms = new ArrayList<>();
                for (GradAlgorithmOptionalStudentProgram optionalPrograms : graduationData.getOptionalGradStatus()) {
                    if (optionalPrograms.getOptionalProgramCode().equals("FI") || optionalPrograms.getOptionalProgramCode().equals("DD") || optionalPrograms.getOptionalProgramCode().equals("FR")) {
                        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
                        studentOptionalProgram.setGraduated(optionalPrograms.getOptionalProgramCompletionDate() != null);
                        studentOptionalProgram.setOptionalProgramCode(optionalPrograms.getOptionalProgramCode());
                        studentOptionalProgram.setProgramCode(graduationStudentRecord.getProgram());
                        studentOptionalProgram.setStudentOptionalProgramData(optionalPrograms.getStudentOptionalProgramData());
                        projectedOptionalPrograms.add(studentOptionalProgram);
                    }
                }
                ExceptionMessage exception = new ExceptionMessage();
                List<ProgramCertificateTranscript> certificateList = reportService.getCertificateList(graduationStudentRecord, graduationData, projectedOptionalPrograms, exception);
                for (ProgramCertificateTranscript certType : certificateList) {
                    reportService.saveStudentCertificateReportJasper(graduationStudentRecord, graduationData, certType, i == 0 && isOverwrite);
                    i++;
                    log.debug("**** Saved Certificates: {} ****", certType.getCertificateTypeCode());
                }
            }
        }
        return i;
    }

    private boolean isGraduated(GraduationStudentRecord graduationStudentRecord, GraduationData graduationData) {
        return graduationData.isGraduated() && graduationStudentRecord.getProgramCompletionDate() != null;
    }

    public byte[] getSchoolReports(List<UUID> uniqueSchoolList, String type) {
        byte[] result = new byte[0];
        for (UUID schoolId : uniqueSchoolList) {
            ca.bc.gov.educ.api.graduation.model.dto.institute.School schoolDetail =  schoolService.getSchoolById(schoolId);
            try {
                List<GraduationStudentRecord> stdList = gradStatusService.getStudentListBySchoolId(schoolId);
                if (schoolDetail != null) {
                    School schoolObj = new School();
                    schoolObj.setSchoolId(schoolDetail.getSchoolId());
                    schoolObj.setMincode(schoolDetail.getMincode());
                    schoolObj.setName(schoolDetail.getDisplayName());
                    ReportData gradReport;
                    switch (type) {
                        case GRADREG:
                            List<Student> gradRegStudents = processStudentList(filterStudentList(stdList, GRADREG), REGALG);
                            gradReport = getReportDataObj(schoolObj, gradRegStudents);
                            return getSchoolReportGradRegReport(gradReport, schoolObj.getMincode());
                        case NONGRADREG:
                            List<Student> nonGradRegStudents = processStudentList(filterStudentList(stdList, NONGRADREG), REGALG);
                            gradReport = getReportDataObj(schoolObj, nonGradRegStudents);
                            return getSchoolReportNonGradRegReport(gradReport, schoolObj.getMincode());
                        case NONGRADPRJ:
                            List<Student> nonGradPrjStudents = processStudentList(filterStudentList(stdList, NONGRADPRJ), TVRRUN);
                            gradReport = getReportDataObj(schoolObj, nonGradPrjStudents);
                            return getSchoolReportStudentNonGradPrjReport(gradReport, schoolObj.getMincode());
                        default:
                            return result;
                    }
                }
            } catch (Exception e) {
                log.error("Failed to generate {} report for Mincode: {} (SchoolId: {}) due to: {}",
                        type, schoolDetail != null ? schoolDetail.getMincode() : null, schoolId, e.getLocalizedMessage());
            }
        }
        return result;
    }

    public Integer createAndStoreSchoolReports(List<UUID> uniqueSchoolList, String type) {
        int numberOfReports = 0;
        for (UUID schoolId : uniqueSchoolList) {
            ca.bc.gov.educ.api.graduation.model.dto.institute.School schoolDetail = null;
            try {
                List<GraduationStudentRecord> stdList = gradStatusService.getStudentListBySchoolId(schoolId);
                if (log.isDebugEnabled()) {
                    int totalStudents = ObjectUtils.defaultIfNull(stdList.size(), 0);
                    String listOfStudents = jsonTransformer.marshall(stdList);
                    log.debug("*** Student List of {} Acquired {}", totalStudents, listOfStudents);
                }
                schoolDetail = schoolService.getSchoolById(schoolId);
                if (schoolDetail != null) {
                    log.debug("*** School Details Acquired {}", schoolDetail.getDisplayName());
                    if (stdList != null && !stdList.isEmpty()) {
                        School schoolObj = new School();
                        schoolObj.setSchoolId(schoolDetail.getSchoolId());
                        schoolObj.setMincode(schoolDetail.getMincode());
                        schoolObj.setName(schoolDetail.getDisplayName());
                        if (TVRRUN.equalsIgnoreCase(type)) {
                            List<Student> nonGradPrjStudents = processStudentList(filterStudentList(stdList, NONGRADPRJ), type);
                            log.debug("*** Process processStudentNonGradPrjReport {} for {} students", schoolObj.getMincode(), nonGradPrjStudents.size());
                            numberOfReports = processStudentNonGradPrjReport(schoolObj, nonGradPrjStudents, schoolId, numberOfReports);
                        } else {
                            List<Student> gradRegStudents = processStudentList(filterStudentList(stdList, GRADREG), type);
                            log.debug("*** Process processGradRegReport {} for {} students", schoolObj.getMincode(), gradRegStudents.size());
                            numberOfReports = processGradRegReport(schoolObj, gradRegStudents, schoolId, numberOfReports);
                            List<Student> nonGradRegStudents = processStudentList(filterStudentList(stdList, NONGRADREG), type);
                            log.debug("*** Process processNonGradRegReport {} for {} students", schoolObj.getMincode(), nonGradRegStudents.size());
                            numberOfReports = processNonGradRegReport(schoolObj, nonGradRegStudents, schoolId, numberOfReports);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to generate {} report for Mincode: {} (SchoolId: {}) due to: {}",
                        type, schoolDetail != null ? schoolDetail.getMincode() : null, schoolId, e.getLocalizedMessage());
            }
        }
        return numberOfReports;
    }

    private List<GraduationStudentRecord> filterStudentList(List<GraduationStudentRecord> stdList, String type) {
        stdList.removeIf(p -> !"CUR".equalsIgnoreCase(p.getStudentStatus()));
        switch (type) {
            case GRADREG -> {
                return stdList.stream().filter(c -> (c.getProgramCompletionDate() != null && !"SCCP".equalsIgnoreCase(c.getProgram()) && EducGraduationApiUtils.parsingTraxDate(c.getProgramCompletionDate()).after(schoolYearDates.getDateFrom()) && EducGraduationApiUtils.parsingTraxDate(c.getProgramCompletionDate()).before(schoolYearDates.getDateTo()))).toList();
            }
            case NONGRADREG, NONGRADPRJ -> {
                return stdList.stream().filter(c -> c.getProgramCompletionDate() == null && ("AD".equalsIgnoreCase(c.getStudentGrade()) || "12".equalsIgnoreCase(c.getStudentGrade()))).toList();
            }
            default -> {
                return stdList;
            }
        }
    }

    private int processGradRegReport(School schoolObj, List<Student> stdList, UUID schoolId, int numberOfReports) {
        int studentsCount = countStudentsForAmalgamatedSchoolReport(schoolId);
        if(studentsCount > 0) {
            ReportData gradReport = getReportDataObj(schoolObj, stdList);
            createAndSaveSchoolReportGradRegReport(gradReport, schoolObj.getMincode(), schoolId);
            numberOfReports++;
        }
        return numberOfReports;
    }

    private int processNonGradRegReport(School schoolObj, List<Student> stdList, UUID schoolId, int numberOfReports) {
        int studentsCount = countStudentsForAmalgamatedSchoolReport(schoolId);
        if(studentsCount > 0) {
            ReportData gradReport = getReportDataObj(schoolObj, stdList);
            createAndSaveSchoolReportNonGradRegReport(gradReport, schoolObj.getMincode(), schoolId);
            numberOfReports++;
        }
        return numberOfReports;
    }

    private int processStudentNonGradPrjReport(School schoolObj, List<Student> stdList, UUID schoolId, int numberOfReports) {
        int studentsCount = countStudentsForAmalgamatedSchoolReport(schoolId);
        if(studentsCount > 0) {
            ReportData nongradProjected = getReportDataObj(schoolObj, stdList);
            createAndSaveSchoolReportStudentNonGradPrjReport(nongradProjected, schoolObj.getMincode(), schoolId);
            numberOfReports++;
        }
        return numberOfReports;
    }

    private int countStudentsForAmalgamatedSchoolReport(UUID schoolId) {
        return restService.get(String.format(educGraduationApiConstants.getGradStudentCountSchoolReport(),
                schoolId), Integer.class, graduationApiClient);
    }

    private ReportData getReportDataObj(School schoolObj, List<Student> stdList) {
        ReportData data = new ReportData();
        schoolObj.setStudents(stdList);
        data.setSchool(schoolObj);
        data.setOrgCode(StringUtils.startsWith(data.getSchool().getMincode(), "098") ? "YU" : "BC");
        data.setIssueDate(EducGraduationApiUtils.formatIssueDateForReportJasper(new java.sql.Date(System.currentTimeMillis()).toString()));
        return data;
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
            //Grad2-1931 - mchintha
            std.setConsumerEducReqt(gsr.getConsumerEducationRequirementMet());
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
                gradData.setGraduationDate(gsr.getProgramCompletionDate() != null ? EducGraduationApiUtils.parsingTraxDateLocalDate(gsr.getProgramCompletionDate()) : null);
                gradData.setHonorsFlag(gsr.getHonoursStanding() != null && gsr.getHonoursStanding().equalsIgnoreCase("Y"));
                std.setGraduationData(gradData);
                std.setNonGradReasons(getNonGradReasons(gsr.getProgram(), gsr.getNonGradReasons()));
                stdPrjList.add(std);
            } else {
                std.setGraduationData(new ca.bc.gov.educ.api.graduation.model.report.GraduationData());
                if (gsr.getStudentProjectedGradData() != null) {
                    ProjectedRunClob projectedClob = (ProjectedRunClob)jsonTransformer.unmarshall(gsr.getStudentProjectedGradData(), ProjectedRunClob.class);
                    std.setNonGradReasons(getNonGradReasons(gsr.getProgram(), projectedClob.getNonGradReasons()));
                    if (!projectedClob.isGraduated())
                        stdPrjList.add(std);
                }
            }
        }
        return stdPrjList;
    }

    private List<NonGradReason> getNonGradReasons(String gradProgramCode, List<GradRequirement> nonGradReasons) {
        List<NonGradReason> nList = new ArrayList<>();
        if (nonGradReasons != null) {
            nonGradReasons.removeIf(a -> ("506".equalsIgnoreCase(a.getTranscriptRule()) || "506".equalsIgnoreCase(a.getRule())) && (StringUtils.isNotBlank(gradProgramCode) && gradProgramCode.contains("1950")));
            for (GradRequirement gR : nonGradReasons) {
                NonGradReason obj = new NonGradReason();
                obj.setCode(gR.getTranscriptRule());
                obj.setDescription(gR.getDescription());
                nList.add(obj);
            }
        }
        return nList;
    }

    private byte[] getSchoolReportGradRegReport(ReportData data, String mincode) {
        ReportOptions options = new ReportOptions();
        options.setReportFile(String.format("%s_%s00_GRADREG", mincode, LocalDate.now().getYear()));
        options.setReportName(String.format("%s_%s00_GRADREG.pdf", mincode, LocalDate.now().getYear()));
        ReportRequest reportParams = new ReportRequest();
        reportParams.setOptions(options);
        reportParams.setData(data);

        return this.restService.post(educGraduationApiConstants.getSchoolGraduation(), reportParams, byte[].class, graduationApiClient);
    }

    @Generated
    private byte[] createAndSaveSchoolReportGradRegReport(ReportData data, String mincode, UUID schoolId) {

        byte[] bytesSAR = getSchoolReportGradRegReport(data, mincode);
        String encodedPdf = getEncodedPdfFromBytes(bytesSAR);
        SchoolReports requestObj = getSchoolReports(schoolId, encodedPdf, GRADREG);
        updateSchoolReport(requestObj);

        return bytesSAR;
    }

    private void updateSchoolReport(SchoolReports requestObj) {
        this.restService.post(educGraduationApiConstants.getUpdateSchoolReport(), requestObj, SchoolReports.class, graduationApiClient);
    }

    private String getEncodedPdfFromBytes(byte[] bytesSAR) {
        byte[] encoded = Base64.encodeBase64(bytesSAR);
        return new String(encoded, StandardCharsets.US_ASCII);
    }

    private byte[] getSchoolReportNonGradRegReport(ReportData data, String mincode) {
        ReportOptions options = new ReportOptions();
        options.setReportFile(String.format("%s_%s00_NONGRADREG", mincode, LocalDate.now().getYear()));
        options.setReportName(String.format("%s_%s00_NONGRADREG.pdf", mincode, LocalDate.now().getYear()));
        ReportRequest reportParams = new ReportRequest();
        reportParams.setOptions(options);
        reportParams.setData(data);

        return this.restService.post(educGraduationApiConstants.getSchoolNonGraduation(), reportParams, byte[].class, graduationApiClient);
    }

    @Generated
    private void createAndSaveSchoolReportNonGradRegReport(ReportData data, String mincode, UUID schoolId) {
        byte[] bytesSAR = getSchoolReportNonGradRegReport(data, mincode);
        String encodedPdf = getEncodedPdfFromBytes(bytesSAR);
        SchoolReports requestObj = getSchoolReports(schoolId, encodedPdf, NONGRADREG);
        updateSchoolReport(requestObj);
    }

    private byte[] getSchoolReportStudentNonGradPrjReport(ReportData data, String mincode) {
        data.setReportTitle("Graduation Records and Achievement Data");
        data.setReportSubTitle("Projected Non-Grad Report for Students in Grade 12 and Adult Students");
        ReportOptions options = new ReportOptions();
        options.setReportFile(String.format("%s_%s00_NONGRADPRJ", mincode, LocalDate.now().getYear()));
        options.setReportName(String.format("%s_%s00_NONGRADPRJ.pdf", mincode, LocalDate.now().getYear()));
        ReportRequest reportParams = new ReportRequest();
        reportParams.setOptions(options);
        reportParams.setData(data);

        return this.restService.post(educGraduationApiConstants.getStudentNonGradProjected(), reportParams, byte[].class, graduationApiClient);
    }

    @Generated
    private byte[] getSchoolReportStudentNonGradReport(ReportData data, String mincode, String accessToken) {
        data.setReportTitle("Graduation Records and Achievement Data");
        data.setReportSubTitle("Projected Non-Grad Report for Students in Grade 12 and Adult Students");
        ReportOptions options = new ReportOptions();
        options.setReportFile(String.format("%s_%s00_NONGRADDISTREP_SC", mincode, LocalDate.now().getYear()));
        options.setReportName(String.format("%s_%s00_NONGRADDISTREP_SC.pdf", mincode, LocalDate.now().getYear()));
        ReportRequest reportParams = new ReportRequest();
        reportParams.setOptions(options);
        reportParams.setData(data);

        return this.restService.post(educGraduationApiConstants.getStudentNonGrad(), reportParams, byte[].class, graduationApiClient);
    }

    private void createAndSaveSchoolReportStudentNonGradPrjReport(ReportData data, String mincode, UUID schoolId) {
        byte[] bytesSAR = getSchoolReportStudentNonGradPrjReport(data, mincode);
        String encodedPdf = getEncodedPdfFromBytes(bytesSAR);
        SchoolReports requestObj = getSchoolReports(schoolId, encodedPdf, NONGRADPRJ);
        updateSchoolReport(requestObj);
    }

    private SchoolReports getSchoolReports(UUID schoolId, String encodedPdf, String nongradreg) {
        SchoolReports requestObj = new SchoolReports();
        requestObj.setReport(encodedPdf);
        requestObj.setSchoolOfRecordId(schoolId);
        requestObj.setReportTypeCode(nongradreg);
        return requestObj;
    }
}
