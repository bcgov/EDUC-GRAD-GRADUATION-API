package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.graduation.model.dto.SchoolClob;
import ca.bc.gov.educ.api.graduation.model.dto.SchoolReports;
import ca.bc.gov.educ.api.graduation.model.dto.institute.YearEndReportRequest;
import ca.bc.gov.educ.api.graduation.model.report.*;
import ca.bc.gov.educ.api.graduation.util.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static ca.bc.gov.educ.api.graduation.constants.ReportingSchoolTypesEnum.SCHOOL_AT_GRAD;
import static ca.bc.gov.educ.api.graduation.constants.ReportingSchoolTypesEnum.SCHOOL_OF_RECORD;

@Service
public class SchoolReportsService extends BaseReportService {

    private static final Logger logger = LoggerFactory.getLogger(SchoolReportsService.class);

    public static final String DISTREP_YE_SC = "DISTREP_YE_SC";
    public static final String DISTREP_YE_SD = "DISTREP_YE_SD";
    public static final String NONGRADDISTREP_SD = "NONGRADDISTREP_SD";
    public static final String DISTREP_SC = "DISTREP_SC";
    public static final String DISTREP_SD = "DISTREP_SD";
    public static final String ADDRESS_LABEL_YE = "ADDRESS_LABEL_YE";
    public static final String ADDRESS_LABEL_SCHL = "ADDRESS_LABEL_SCHL";
    public static final String ADDRESS_LABEL_PSI = "ADDRESS_LABEL_PSI";

    private static final String SCHOOL_REPORTS_CREATED = "***** {} of School Reports Created *****";
    private static final String SCHOOL_DISTRICT_REPORTS_CREATED = "***** {} of School Districts Reports Created *****";
    private static final String SCHOOL_LABEL_REPORTS_CREATED = "***** {} of School Labels Reports Created *****";


    WebClient webClient;
    ReportService reportService;
    SchoolService schoolService;
    DistrictReportService districtReportService;
    TokenUtils tokenUtils;
    EducGraduationApiConstants educGraduationApiConstants;
    RESTService restService;
    JsonTransformer jsonTransformer;

    @Autowired
    public SchoolReportsService(WebClient webClient, ReportService reportService, SchoolService schoolService, TokenUtils tokenUtils, EducGraduationApiConstants educGraduationApiConstants, RESTService restService, JsonTransformer jsonTransformer, DistrictReportService districtReportService) {
        this.webClient = webClient;
        this.reportService = reportService;
        this.schoolService = schoolService;
        this.tokenUtils = tokenUtils;
        this.educGraduationApiConstants = educGraduationApiConstants;
        this.restService = restService;
        this.jsonTransformer = jsonTransformer;
        this.districtReportService = districtReportService;
    }

    public byte[] getSchoolDistrictYearEndReports(String slrt, String drt, String srt) throws IOException {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport();
        List<InputStream> pdfs = new ArrayList<>();
        createAndStoreReports(reportGradStudentDataList, slrt, drt, srt, pdfs);
        return mergeDocuments(pdfs);
    }

    public byte[] getSchoolDistrictReports(List<ReportGradStudentData> reportGradStudentDataList, String slrt, String drt, String srt) throws IOException {
        List<InputStream> pdfs = new ArrayList<>();
        if(ADDRESS_LABEL_SCHL.equalsIgnoreCase(slrt)) {
            createAndStoreSchoolLabelsReports(ADDRESS_LABEL_SCHL, reportGradStudentDataList, pdfs);
        }
        if(DISTREP_SD.equalsIgnoreCase(drt)) {
            districtReportService.createAndStoreDistrictReports(DISTREP_SD, reportGradStudentDataList, pdfs);
        }
        if(DISTREP_SC.equalsIgnoreCase(srt)) {
            createAndStoreSchoolReports(DISTREP_SC, reportGradStudentDataList, pdfs);
        }
        return mergeDocuments(pdfs);
    }

    public byte[] getSchoolDistrictReports(String slrt, String drt, String srt) throws IOException {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolReport();
        return getSchoolDistrictReports(reportGradStudentDataList, slrt, drt, srt);
    }

    public byte[] getSchoolYearEndReports() throws IOException {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport();
        List<InputStream> pdfs = new ArrayList<>();
        createAndStoreSchoolReports(DISTREP_YE_SC, reportGradStudentDataList, pdfs);
        return mergeDocuments(pdfs);
    }

    public byte[] getSchoolReports() throws IOException {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolReport();
        List<InputStream> pdfs = new ArrayList<>();
        createAndStoreSchoolReports(DISTREP_SC, reportGradStudentDataList, pdfs);
        return mergeDocuments(pdfs);
    }

    @Generated
    public Integer createAndStoreSchoolDistrictYearEndReports(String slrt, String drt, String srt) {
        logger.debug("***** Get Students for School Year End Reports Starts *****");
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport();
        logger.debug("***** {} Students Retrieved *****", reportGradStudentDataList.size());
        return createAndStoreReports(reportGradStudentDataList, slrt, drt, srt, null);
    }

    private Integer createAndStoreReports(List<ReportGradStudentData> reportGradStudentDataList, String slrt, String drt, String srt, List<InputStream> pdfs) {
        int schoolLabelsCount = 0;
        if(StringUtils.isNotBlank(slrt)) {
            schoolLabelsCount += createAndStoreSchoolLabelsReports(slrt, reportGradStudentDataList, pdfs);
            logger.debug(SCHOOL_LABEL_REPORTS_CREATED, schoolLabelsCount);
        }
        int districtReportsCount = 0;
        if(StringUtils.isNotBlank(drt)) {
            districtReportsCount += districtReportService.createAndStoreDistrictReports(drt, reportGradStudentDataList, pdfs);
            logger.debug(SCHOOL_DISTRICT_REPORTS_CREATED, districtReportsCount);
        }
        int schoolReportsCount = 0;
        if(StringUtils.isNotBlank(srt)) {
            schoolReportsCount += createAndStoreSchoolReports(srt, reportGradStudentDataList, pdfs);
            logger.debug(SCHOOL_REPORTS_CREATED, schoolReportsCount);
        }
        return schoolLabelsCount + districtReportsCount + schoolReportsCount;
    }

    @Generated
    public Integer createAndStoreSchoolDistrictYearEndReports(String slrt, String drt, String srt, YearEndReportRequest yearEndReportRequest) {
        logger.debug("***** Get Students for School Year End Reports Starts *****");
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport(yearEndReportRequest);
        logger.debug("***** {} Students Retrieved *****", reportGradStudentDataList.size());
        return createAndStoreReports(reportGradStudentDataList, slrt, drt, srt, null);
    }

    @Generated
    public Integer createAndStoreSchoolDistrictReports(List<ReportGradStudentData> reportGradStudentDataList, String slrt, String drt, String srt) {
        logger.debug("***** Get Students for School Monthly Reports Starts *****");
        return createAndStoreReports(reportGradStudentDataList, slrt, drt, srt, null);
    }

    public Integer createAndStoreSchoolDistrictReports(String slrt, String drt, String srt) {
        logger.debug("***** Get Students for School Monthly Reports Starts *****");
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolReport();
        return createAndStoreSchoolDistrictReports(reportGradStudentDataList, slrt, drt, srt);
    }

    public Integer createAndStoreSchoolReports(String reportType) {
        List<ReportGradStudentData> reportGradStudentDataList;
        if(DISTREP_YE_SC.equalsIgnoreCase(reportType)) {
            reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport();
        } else {
            reportGradStudentDataList = reportService.getStudentsForSchoolReport();
        }
        return createAndStoreSchoolReports(reportType, reportGradStudentDataList, null);
    }

    public byte[] getSchoolLabelsReportsFromSchools(String reportType, List<School> schools) throws IOException {
        List<InputStream> pdfs = new ArrayList<>();
        createAndStoreSchoolLabelsReportsFromSchools(reportType, schools, pdfs);
        return mergeDocuments(pdfs);
    }

    public Integer createAndStoreSchoolLabelsReportsFromSchools(String reportType, List<School> schools, List<InputStream> pdfs) {
        Integer reportsCount = 0;
        ReportRequest reportRequest = buildSchoolLabelsReportRequest(schools);
        byte[] reportAsBytes = getSchoolLabelsReportJasper(reportRequest);
        if (reportAsBytes != null && pdfs != null) {
            ByteArrayInputStream is = new ByteArrayInputStream(reportAsBytes);
            pdfs.add(is);
        }
        if (pdfs == null) {
            UUID schoolLabelMinCode = (schools != null && schools.size() == 1) ? UUID.fromString(schools.get(0).getSchoolId()) :UUID.fromString("00000000-0000-0000-0000-000000000000");
            saveSchoolOrLabelsReport(schoolLabelMinCode, reportType, reportAsBytes);
        }
        reportsCount++;
        return reportsCount;
    }

    private Integer createAndStoreSchoolLabelsReports(String reportType, List<ReportGradStudentData> reportGradStudentDataList, List<InputStream> pdfs) {
        Map<String, School> schoolMap = new HashMap<>();
        for (ReportGradStudentData reportGradStudentData : reportGradStudentDataList) {
            populateSchoolObjectByReportGradStudentData(schoolMap, reportGradStudentData);
        }
        List<School> schools = new ArrayList<>(schoolMap.values());
        return createAndStoreSchoolLabelsReportsFromSchools(reportType, schools, pdfs);
    }

    @Generated
    private Integer createAndStoreSchoolReports(String reportType, List<ReportGradStudentData> reportGradStudentDataList, List<InputStream> pdfs) {
        Integer reportsCount = 0;
        Map<String, School> newCredentialsSchoolMap = new HashMap<>();
        for (ReportGradStudentData reportGradStudentData : reportGradStudentDataList) {
            School school = populateSchoolObjectByReportGradStudentData(newCredentialsSchoolMap, reportGradStudentData);
            Student student = processNewCredentialsSchoolMap(reportGradStudentData);
            if (student != null && !school.getStudents().contains(student)) {
                school.getStudents().add(student);
            } else if (student != null) {
                for(Student st: school.getStudents()) {
                    if(st.getPen().equals(student.getPen())) {
                        st.getGraduationStatus().setCertificates(reportGradStudentData.getCertificateTypeCode());
                    }
                }
            }
        }
        Map<String, School> issuedTranscriptsSchoolMap = new HashMap<>();
        for (ReportGradStudentData reportGradStudentData : reportGradStudentDataList) {
            School school = populateSchoolObjectByReportGradStudentData(issuedTranscriptsSchoolMap, reportGradStudentData);
            Student student = processIssuedTranscriptsSchoolMap(reportGradStudentData);
            if (student != null && !school.getStudents().contains(student)) {
                school.getStudents().add(student);
            }
        }
        if (issuedTranscriptsSchoolMap.size() > newCredentialsSchoolMap.size()) {
            for (var entry : issuedTranscriptsSchoolMap.entrySet()) {
                School transcriptSchool = entry.getValue();
                ReportRequest reportRequest = buildSchoolYearEndReportRequest(transcriptSchool);
                ReportData reportData = new ReportData();
                reportData.setSchool(transcriptSchool);
                reportData.setOrgCode(getReportOrgCode(transcriptSchool.getMincode()));
                reportRequest.getDataMap().put("issuedTranscriptsReportData", reportData);
                School newCredentialsSchool = newCredentialsSchoolMap.get(transcriptSchool.getMincode());
                if (newCredentialsSchool != null) {
                    reportData = new ReportData();
                    reportData.setSchool(newCredentialsSchool);
                    reportData.setOrgCode(getReportOrgCode(newCredentialsSchool.getMincode()));
                    reportRequest.getDataMap().put("newCredentialsReportData", reportData);
                }
                byte[] reportAsBytes = getSchoolYearEndReportJasper(reportRequest);
                if (reportAsBytes != null && pdfs != null) {
                    ByteArrayInputStream is = new ByteArrayInputStream(reportAsBytes);
                    pdfs.add(is);
                }
                if (pdfs == null) {
                    saveSchoolOrLabelsReport(UUID.fromString(reportRequest.getData().getSchool().getSchoolId()), reportType, reportAsBytes);
                }
                reportsCount++;
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
                if (transcriptSchool != null) {
                    reportData = new ReportData();
                    reportData.setSchool(transcriptSchool);
                    reportData.setOrgCode(getReportOrgCode(transcriptSchool.getMincode()));
                    reportRequest.getDataMap().put("issuedTranscriptsReportData", reportData);
                }
                byte[] reportAsBytes = getSchoolYearEndReportJasper(reportRequest);
                if (reportAsBytes != null && pdfs != null) {
                    ByteArrayInputStream is = new ByteArrayInputStream(reportAsBytes);
                    pdfs.add(is);
                }
                if (pdfs == null) {
                    saveSchoolOrLabelsReport(UUID.fromString(reportRequest.getData().getSchool().getSchoolId()), reportType, reportAsBytes);
                }
                reportsCount++;
            }
        }
        return reportsCount;
    }

    @Generated
    private void saveSchoolOrLabelsReport(UUID schoolId, String reportType, byte[] reportAsBytes) {
        String encodedPdf = getEncodedPdfFromBytes(reportAsBytes);
        SchoolReports schoolReports = getSchoolReports(schoolId, encodedPdf, reportType);
        updateSchoolReport(schoolReports);
    }

    private ReportRequest buildSchoolYearEndReportRequest(School school) {
        ReportRequest reportRequest = new ReportRequest();
        ReportOptions reportOptions = new ReportOptions();
        reportOptions.setReportName("schooldistributionyearend");
        reportOptions.setReportFile(String.format("%s School Distribution Report Year End.pdf", school.getMincode()));
        reportRequest.setOptions(reportOptions);
        ReportData headerData = new ReportData();
        School headerSchool = new School();
        headerSchool.setSchoolId(school.getSchoolId());
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

    private ReportRequest buildSchoolLabelsReportRequest(List<School> schools) {
        ReportRequest reportRequest = new ReportRequest();
        ReportOptions reportOptions = new ReportOptions();
        reportOptions.setReportName("schoollabels");
        reportOptions.setReportFile("School Labels.pdf");
        reportRequest.setOptions(reportOptions);
        ReportData data = new ReportData();
        data.setIssueDate(new Date());
        data.setSchools(schools);
        reportRequest.setData(data);
        return reportRequest;
    }

    private String getReportOrgCode(String mincode) {
        return (StringUtils.startsWith(mincode, "098") ? "YU" : "BC");
    }

    private School populateSchoolObjectByReportGradStudentData(ReportGradStudentData reportGradStudentData) {
        //if not year end, reportingSchoolTypeCode isn't set, so check old way
        String schoolId = reportGradStudentData.getSchoolOfRecordId();
        if(reportGradStudentData.getReportingSchoolTypeCode() != null && reportGradStudentData.getReportingSchoolTypeCode().equals(SCHOOL_AT_GRAD.name())) {
            schoolId = reportGradStudentData.getSchoolAtGradId();
        } else if (reportGradStudentData.getReportingSchoolTypeCode() != null && reportGradStudentData.getReportingSchoolTypeCode().equals(SCHOOL_OF_RECORD.name())) {
            schoolId = reportGradStudentData.getSchoolOfRecordId();
        }
        School school = new School();
        school.setStudents(new ArrayList<>());

        SchoolClob schoolClob = schoolService.getSchoolClob(schoolId);
        if (schoolClob != null) {
            school.setDistno(StringUtils.substring(schoolClob.getMinCode(), 0, 3));
            school.setSchoolId(schoolId);
            school.setMincode(schoolClob.getMinCode());
            school.setName(schoolClob.getSchoolName());
            school.setTypeBanner("Principal");
            Address address = new Address();
            address.setStreetLine1(schoolClob.getAddress1());
            address.setStreetLine2(schoolClob.getAddress2());
            address.setCity(schoolClob.getCity());
            address.setRegion(schoolClob.getProvCode());
            address.setCountry(schoolClob.getCountryCode());
            address.setCode(schoolClob.getPostal());
            school.setAddress(address);
            return school;
        }
        return school;
    }

    private School populateSchoolObjectByReportGradStudentData(Map<String, School> schoolMap, ReportGradStudentData reportGradStudentData) {
        //if not year end, reportingSchoolTypeCode isn't set, so check old way
        String schoolId = reportGradStudentData.getSchoolOfRecordId();
        if(reportGradStudentData.getReportingSchoolTypeCode() != null && reportGradStudentData.getReportingSchoolTypeCode().equals(SCHOOL_AT_GRAD.name())) {
            schoolId = reportGradStudentData.getSchoolAtGradId();
        } else if (reportGradStudentData.getReportingSchoolTypeCode() != null && reportGradStudentData.getReportingSchoolTypeCode().equals(SCHOOL_OF_RECORD.name())) {
            schoolId = reportGradStudentData.getSchoolOfRecordId();
        }
        //<--
        School school = schoolMap.get(schoolId);
        if (school == null) {
            school = populateSchoolObjectByReportGradStudentData(reportGradStudentData);
            schoolMap.put(schoolId, school);
        }
        return school;
    }

    private Student processNewCredentialsSchoolMap(ReportGradStudentData reportGradStudentData) {
        if (StringUtils.isNotBlank(reportGradStudentData.getCertificateTypeCode())) {
            return populateStudentObjectByReportGradStudentData(reportGradStudentData);
        }
        return null;
    }

    private Student processIssuedTranscriptsSchoolMap(ReportGradStudentData reportGradStudentData) {
        String paperType = reportGradStudentData.getPaperType();
        String transcriptTypeCode = reportGradStudentData.getTranscriptTypeCode();
        String certificateTypeCode = reportGradStudentData.getCertificateTypeCode();
        logger.debug("Processing school {} transcript {} for student {} and paper type {}", reportGradStudentData.getMincode(), transcriptTypeCode, reportGradStudentData.getPen(), paperType);
        if("YED4".equalsIgnoreCase(paperType) || StringUtils.isBlank(certificateTypeCode)) {
            return populateStudentObjectByReportGradStudentData(reportGradStudentData);
        }
        return null;
    }

    private Student populateStudentObjectByReportGradStudentData(ReportGradStudentData reportGradStudentData) {
        Student student = new Student();
        Pen pen = new Pen();
        pen.setPen(reportGradStudentData.getPen());
        pen.setEntityID(reportGradStudentData.getGraduationStudentRecordId().toString());
        student.setPen(pen);
        student.setFirstName(reportGradStudentData.getFirstName());
        student.setMiddleName(reportGradStudentData.getMiddleName());
        student.setLastName(reportGradStudentData.getLastName());
        student.setGradProgram(reportGradStudentData.getProgramCode());
        student.setLastUpdateDate(reportGradStudentData.getUpdateDate());

        GraduationStatus gradStatus = new GraduationStatus();
        gradStatus.setProgramCompletionDate(reportGradStudentData.getProgramCompletionDate());
        //--> Revert code back to school of record GRAD2-2758
        /** gradStatus.setSchoolOfRecord(StringUtils.isBlank(reportGradStudentData.getMincodeAtGrad()) ? reportGradStudentData.getMincode() : reportGradStudentData.getMincodeAtGrad()); **/
        gradStatus.setSchoolOfRecord(reportGradStudentData.getMincode());
        //<--
        gradStatus.setSchoolAtGrad(reportGradStudentData.getMincodeAtGrad());
        gradStatus.setProgramName(reportGradStudentData.getProgramCode());
        gradStatus.setCertificates(reportGradStudentData.getCertificateTypeCode());
        student.setGraduationStatus(gradStatus);

        ca.bc.gov.educ.api.graduation.model.report.GraduationData gradData = new ca.bc.gov.educ.api.graduation.model.report.GraduationData();
        gradData.setGraduationDate(EducGraduationApiUtils.parseDateLocalDate(reportGradStudentData.getProgramCompletionDate()));
        gradData.setProgramCodes(List.of(reportGradStudentData.getProgramCode()));
        gradData.setProgramNames(List.of(reportGradStudentData.getProgramName()));
        student.setGraduationData(gradData);

        return student;
    }

    private void updateSchoolReport(SchoolReports requestObj) {
        this.restService.post(educGraduationApiConstants.getUpdateSchoolReport(),
                requestObj,
                SchoolReports.class);
    }

    private byte[] getSchoolYearEndReportJasper(ReportRequest reportRequest) {
        return restService.post(educGraduationApiConstants.getSchoolDistributionYearEnd(), reportRequest, byte[].class);
    }

    private byte[] getSchoolLabelsReportJasper(ReportRequest reportRequest) {
        return restService.post(educGraduationApiConstants.getSchoolLabels(), reportRequest, byte[].class);
    }

    private SchoolReports getSchoolReports(UUID schoolId, String encodedPdf, String reportType) {
        SchoolReports requestObj = new SchoolReports();
        requestObj.setReport(encodedPdf);
        requestObj.setSchoolOfRecordId(schoolId);
        requestObj.setReportTypeCode(reportType);
        return requestObj;
    }
}
