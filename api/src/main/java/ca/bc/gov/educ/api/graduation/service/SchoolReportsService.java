package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.DistrictTrax;
import ca.bc.gov.educ.api.graduation.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.graduation.model.dto.SchoolReports;
import ca.bc.gov.educ.api.graduation.model.dto.SchoolTrax;
import ca.bc.gov.educ.api.graduation.model.report.*;
import ca.bc.gov.educ.api.graduation.util.*;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class SchoolReportsService {

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
    TokenUtils tokenUtils;
    EducGraduationApiConstants educGraduationApiConstants;
    RESTService restService;
    JsonTransformer jsonTransformer;

    @Autowired
    public SchoolReportsService(WebClient webClient, ReportService reportService, SchoolService schoolService, TokenUtils tokenUtils, EducGraduationApiConstants educGraduationApiConstants, RESTService restService, JsonTransformer jsonTransformer) {
        this.webClient = webClient;
        this.reportService = reportService;
        this.schoolService = schoolService;
        this.tokenUtils = tokenUtils;
        this.educGraduationApiConstants = educGraduationApiConstants;
        this.restService = restService;
        this.jsonTransformer = jsonTransformer;
    }

    public byte[] getSchoolDistrictYearEndReports(String accessToken, String slrt, String drt, String srt) {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport(accessToken);
        List<InputStream> pdfs = new ArrayList<>();
        createAndStoreReports(reportGradStudentDataList, accessToken, slrt, drt, srt, pdfs);
        return mergeDocuments(pdfs);
    }

    public byte[] getSchoolDistrictReports(String accessToken, List<ReportGradStudentData> reportGradStudentDataList, String slrt, String drt, String srt) {
        List<InputStream> pdfs = new ArrayList<>();
        if(ADDRESS_LABEL_SCHL.equalsIgnoreCase(slrt)) {
            createAndStoreSchoolLabelsReports(ADDRESS_LABEL_SCHL, reportGradStudentDataList, accessToken, pdfs);
        }
        if(DISTREP_SD.equalsIgnoreCase(drt)) {
            createAndStoreDistrictReports(DISTREP_SD, reportGradStudentDataList, accessToken, pdfs);
        }
        if(DISTREP_SC.equalsIgnoreCase(srt)) {
            createAndStoreSchoolReports(DISTREP_SC, reportGradStudentDataList, accessToken, pdfs);
        }
        return mergeDocuments(pdfs);
    }

    public byte[] getSchoolDistrictReports(String accessToken, String slrt, String drt, String srt) {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolReport(accessToken);
        return getSchoolDistrictReports(accessToken, reportGradStudentDataList, slrt, drt, srt);
    }

    public byte[] getSchoolYearEndReports(String accessToken) {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport(accessToken);
        List<InputStream> pdfs = new ArrayList<>();
        createAndStoreSchoolReports(DISTREP_YE_SC, reportGradStudentDataList, accessToken, pdfs);
        return mergeDocuments(pdfs);
    }

    public byte[] getSchoolReports(String accessToken) {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolReport(accessToken);
        List<InputStream> pdfs = new ArrayList<>();
        createAndStoreSchoolReports(DISTREP_SC, reportGradStudentDataList, accessToken, pdfs);
        return mergeDocuments(pdfs);
    }

    public byte[] getDistrictYearEndReports(String accessToken) {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport(accessToken);
        List<InputStream> pdfs = new ArrayList<>();
        createAndStoreDistrictReports(DISTREP_YE_SD, reportGradStudentDataList, accessToken, pdfs);
        return mergeDocuments(pdfs);
    }

    public byte[] getDistrictYearEndNonGradReports(String accessToken) {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolNonGradYearEndReport(accessToken);
        List<InputStream> pdfs = new ArrayList<>();
        createAndStoreDistrictReports(NONGRADDISTREP_SD, reportGradStudentDataList, accessToken, pdfs);
        return mergeDocuments(pdfs);
    }

    public byte[] getDistrictReports(String accessToken) {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolReport(accessToken);
        List<InputStream> pdfs = new ArrayList<>();
        createAndStoreDistrictReports(DISTREP_SD, reportGradStudentDataList, accessToken, pdfs);
        return mergeDocuments(pdfs);
    }

    @Generated
    public Integer createAndStoreSchoolDistrictYearEndReports(String accessToken, String slrt, String drt, String srt) {
        logger.debug("***** Get Students for School Year End Reports Starts *****");
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport(accessToken);
        logger.debug("***** {} Students Retrieved *****", reportGradStudentDataList.size());
        return createAndStoreReports(reportGradStudentDataList, accessToken, slrt, drt, srt, null);
    }

    private Integer createAndStoreReports(List<ReportGradStudentData> reportGradStudentDataList, String accessToken, String slrt, String drt, String srt, List<InputStream> pdfs) {
        int schoolLabelsCount = 0;
        if(StringUtils.isNotBlank(slrt)) {
            schoolLabelsCount += createAndStoreSchoolLabelsReports(slrt, reportGradStudentDataList, accessToken, pdfs);
            logger.debug(SCHOOL_LABEL_REPORTS_CREATED, schoolLabelsCount);
        }
        int districtReportsCount = 0;
        if(StringUtils.isNotBlank(drt)) {
            districtReportsCount += createAndStoreDistrictReports(drt, reportGradStudentDataList, accessToken, pdfs);
            logger.debug(SCHOOL_DISTRICT_REPORTS_CREATED, districtReportsCount);
        }
        int schoolReportsCount = 0;
        if(StringUtils.isNotBlank(srt)) {
            schoolReportsCount += createAndStoreSchoolReports(srt, reportGradStudentDataList, accessToken, pdfs);
            logger.debug(SCHOOL_REPORTS_CREATED, schoolReportsCount);
        }
        return schoolLabelsCount + districtReportsCount + schoolReportsCount;
    }

    @Generated
    public Integer createAndStoreSchoolDistrictYearEndReports(String accessToken, String slrt, String drt, String srt, List<String> schools) {
        logger.debug("***** Get Students for School Year End Reports Starts *****");
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport(accessToken, schools);
        logger.debug("***** {} Students Retrieved *****", reportGradStudentDataList.size());
        if(schools != null && !schools.isEmpty()) {
            boolean isDistrictSchool = schools.get(0).length() == 3;
            if(isDistrictSchool) {
                reportGradStudentDataList.removeIf(st -> ((StringUtils.isBlank(st.getMincodeAtGrad()) || StringUtils.equals(st.getMincode(), st.getMincodeAtGrad())) && !schools.contains(StringUtils.substring(st.getMincode(), 0, 3))));
                reportGradStudentDataList.removeIf(st -> ((StringUtils.isNotBlank(st.getMincodeAtGrad()) && !StringUtils.equals(st.getMincode(), st.getMincodeAtGrad())) && !schools.contains(StringUtils.substring(st.getMincodeAtGrad(), 0, 3))));
            }
            boolean isSchoolSchool = schools.get(0).length() > 3;
            if(isSchoolSchool) {
                reportGradStudentDataList.removeIf(st -> ((StringUtils.isBlank(st.getMincodeAtGrad()) || StringUtils.equals(st.getMincode(), st.getMincodeAtGrad())) && !schools.contains(StringUtils.trimToEmpty(st.getMincode()))));
                reportGradStudentDataList.removeIf(st -> ((StringUtils.isNotBlank(st.getMincodeAtGrad()) && !StringUtils.equals(st.getMincode(), st.getMincodeAtGrad())) && !schools.contains(StringUtils.trimToEmpty(st.getMincodeAtGrad()))));
            }
        }
        return createAndStoreReports(reportGradStudentDataList, accessToken, slrt, drt, srt, null);
    }

    @Generated
    public Integer createAndStoreSchoolDistrictReports(String accessToken, List<ReportGradStudentData> reportGradStudentDataList, String slrt, String drt, String srt) {
        logger.debug("***** Get Students for School Monthly Reports Starts *****");
        return createAndStoreReports(reportGradStudentDataList, accessToken, slrt, drt, srt, null);
    }

    public Integer createAndStoreSchoolDistrictReports(String accessToken, String slrt, String drt, String srt) {
        logger.debug("***** Get Students for School Monthly Reports Starts *****");
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolReport(accessToken);
        return createAndStoreSchoolDistrictReports(accessToken, reportGradStudentDataList, slrt, drt, srt);
    }

    public Integer createAndStoreDistrictReports(String reportType, String accessToken) {
        List<ReportGradStudentData> reportGradStudentDataList;
        if(DISTREP_YE_SD.equalsIgnoreCase(reportType)) {
            reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport(accessToken);
        } else if(NONGRADDISTREP_SD.equalsIgnoreCase(reportType)) {
            reportGradStudentDataList = reportService.getStudentsForSchoolNonGradYearEndReport(accessToken);
        } else{
            reportGradStudentDataList = reportService.getStudentsForSchoolReport(accessToken);
        }
        return createAndStoreDistrictReports(reportType, reportGradStudentDataList, accessToken, null);
    }

    public Integer createAndStoreSchoolReports(String reportType, String accessToken) {
        List<ReportGradStudentData> reportGradStudentDataList;
        if(DISTREP_YE_SC.equalsIgnoreCase(reportType)) {
            reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport(accessToken);
        } else {
            reportGradStudentDataList = reportService.getStudentsForSchoolReport(accessToken);
        }
        return createAndStoreSchoolReports(reportType, reportGradStudentDataList, accessToken, null);
    }

    public byte[] getSchoolLabelsReportsFromSchools(String reportType, List<School> schools, String accessToken) {
        List<InputStream> pdfs = new ArrayList<>();
        createAndStoreSchoolLabelsReportsFromSchools(reportType, schools, accessToken, pdfs);
        return mergeDocuments(pdfs);
    }

    public Integer createAndStoreSchoolLabelsReportsFromSchools(String reportType, List<School> schools, String accessToken, List<InputStream> pdfs) {
        Integer reportsCount = 0;
        ReportRequest reportRequest = buildSchoolLabelsReportRequest(schools);
        accessToken = getAccessToken(accessToken).getLeft();
        byte[] reportAsBytes = getSchoolLabelsReportJasper(reportRequest, accessToken);
        if (reportAsBytes != null && pdfs != null) {
            ByteArrayInputStream is = new ByteArrayInputStream(reportAsBytes);
            pdfs.add(is);
        }
        if (pdfs == null) {
            String schoolLabelMinCode = (schools != null && schools.size() == 1) ? schools.get(0).getMincode() : "000000000";
            saveDistrictOrSchoolOrLabelsReport(accessToken, schoolLabelMinCode, reportType, reportAsBytes);
        }
        reportsCount++;
        return reportsCount;
    }

    private Integer createAndStoreSchoolLabelsReports(String reportType, List<ReportGradStudentData> reportGradStudentDataList, String accessToken, List<InputStream> pdfs) {
        Map<String, School> schoolMap = new HashMap<>();
        for (ReportGradStudentData reportGradStudentData : reportGradStudentDataList) {
            populateSchoolObjectByReportGradStudentData(schoolMap, reportGradStudentData);
        }
        List<School> schools = new ArrayList<>(schoolMap.values());
        return createAndStoreSchoolLabelsReportsFromSchools(reportType, schools, accessToken, pdfs);
    }

    @Generated
    private Integer createAndStoreSchoolReports(String reportType, List<ReportGradStudentData> reportGradStudentDataList, String accessToken, List<InputStream> pdfs) {
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
                accessToken = getAccessToken(accessToken).getLeft();
                byte[] reportAsBytes = getSchoolYearEndReportJasper(reportRequest, accessToken);
                if (reportAsBytes != null && pdfs != null) {
                    ByteArrayInputStream is = new ByteArrayInputStream(reportAsBytes);
                    pdfs.add(is);
                }
                if (pdfs == null) {
                    saveDistrictOrSchoolOrLabelsReport(accessToken, reportRequest.getData().getSchool().getMincode(), reportType, reportAsBytes);
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
                accessToken = getAccessToken(accessToken).getLeft();
                byte[] reportAsBytes = getSchoolYearEndReportJasper(reportRequest, accessToken);
                if (reportAsBytes != null && pdfs != null) {
                    ByteArrayInputStream is = new ByteArrayInputStream(reportAsBytes);
                    pdfs.add(is);
                }
                if (pdfs == null) {
                    saveDistrictOrSchoolOrLabelsReport(accessToken, reportRequest.getData().getSchool().getMincode(), reportType, reportAsBytes);
                }
                reportsCount++;
            }
        }
        return reportsCount;
    }

    @Generated
    private Integer createAndStoreDistrictReports(String reportType, List<ReportGradStudentData> reportGradStudentDataList, String accessToken, List<InputStream> pdfs) {
        Integer reportsCount = 0;
        Map<School, List<School>> districtSchoolsMap = new HashMap<>();
        for (ReportGradStudentData reportGradStudentData : reportGradStudentDataList) {
            String mincode = StringUtils.isBlank(reportGradStudentData.getMincodeAtGrad()) ? reportGradStudentData.getMincode() : reportGradStudentData.getMincodeAtGrad();
            String schoolCategoryCode = reportService.getSchoolCategoryCode(accessToken, mincode);
            if(!StringUtils.equalsAnyIgnoreCase(schoolCategoryCode, "02")) {
                School district = populateDistrictObjectByReportGradStudentData(districtSchoolsMap, reportGradStudentData);
                processDistrictSchoolMap(districtSchoolsMap.get(district), reportGradStudentData);
            }
        }
        for (var entry : districtSchoolsMap.entrySet()) {
            School district = entry.getKey();
            List<School> schools = entry.getValue();
            ReportRequest reportRequest = buildDistrictYearEndReportRequest(district);
            reportRequest.getData().getSchools().addAll(schools);
            accessToken = getAccessToken(accessToken).getLeft();
            byte[] reportAsBytes;
            if(DISTREP_YE_SD.equalsIgnoreCase(reportType)) {
                reportAsBytes = getDistrictYearEndReportJasper(reportRequest, accessToken);
            } else {
                reportAsBytes = getDistrictYearEndNonGradReportJasper(reportRequest, accessToken);
            }
            if (reportAsBytes != null && pdfs != null) {
                ByteArrayInputStream is = new ByteArrayInputStream(reportAsBytes);
                pdfs.add(is);
            }
            if (pdfs == null) {
                saveDistrictOrSchoolOrLabelsReport(accessToken, reportRequest.getData().getSchool().getMincode(), reportType, reportAsBytes);
            }
            reportsCount++;
        }
        return reportsCount;
    }

    @Generated
    private void saveDistrictOrSchoolOrLabelsReport(String accessToken, String mincode, String reportType, byte[] reportAsBytes) {
        String encodedPdf = getEncodedPdfFromBytes(reportAsBytes);
        SchoolReports schoolReports = getSchoolReports(mincode, encodedPdf, reportType);
        updateSchoolReport(schoolReports);
    }

    @Generated
    private byte[] getSchoolYearEndReportJasper(ReportRequest reportRequest, String accessToken) {
        return webClient.post().uri(educGraduationApiConstants.getSchoolDistributionYearEnd())
                .headers(h -> {
                            h.setBearerAuth(accessToken);
                            h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                        }
                ).body(BodyInserters.fromValue(reportRequest)).retrieve().bodyToMono(byte[].class).block();
    }

    @Generated
    private byte[] getSchoolLabelsReportJasper(ReportRequest reportRequest, String accessToken) {
        return webClient.post().uri(educGraduationApiConstants.getSchoolLabels())
                .headers(h -> {
                            h.setBearerAuth(accessToken);
                            h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                        }
                ).body(BodyInserters.fromValue(reportRequest)).retrieve().bodyToMono(byte[].class).block();
    }

    @Generated
    private byte[] getDistrictYearEndReportJasper(ReportRequest reportRequest, String accessToken) {
        return webClient.post().uri(educGraduationApiConstants.getDistrictDistributionYearEnd())
                .headers(h -> {
                            h.setBearerAuth(accessToken);
                            h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                        }
                ).body(BodyInserters.fromValue(reportRequest)).retrieve().bodyToMono(byte[].class).block();
    }

    @Generated
    private byte[] getDistrictYearEndNonGradReportJasper(ReportRequest reportRequest, String accessToken) {
        return webClient.post().uri(educGraduationApiConstants.getDistrictDistributionYearEndNonGrad())
                .headers(h -> {
                            h.setBearerAuth(accessToken);
                            h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                        }
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

    private School populateDistrictObjectByReportGradStudentData(Map<School, List<School>> districtSchoolsMap, ReportGradStudentData reportGradStudentData) {
        //district data, not school
        String mincode = StringUtils.isBlank(reportGradStudentData.getMincodeAtGrad()) ? reportGradStudentData.getMincode() : reportGradStudentData.getMincodeAtGrad();
        String distcode = StringUtils.substring(mincode, 0, 3);
        boolean addNewDistrict = true;
        School district = null;
        for (var entry : districtSchoolsMap.entrySet()) {
            School currentDistrict = entry.getKey();
            if (StringUtils.equals(distcode, currentDistrict.getDistno())) {
                addNewDistrict = false;
                district = currentDistrict;
            }
        }
        if (addNewDistrict) {
            DistrictTrax districtTrax = schoolService.getTraxDistrictDetails(distcode);
            district = new School();
            district.setDistno(distcode);
            district.setMincode(distcode);
            district.setName(districtTrax != null ? districtTrax.getDistrictName() : reportGradStudentData.getDistrictName());
            districtSchoolsMap.put(district, new ArrayList<>());
        }
        return district;
    }

    private School populateSchoolObjectByReportGradStudentData(ReportGradStudentData reportGradStudentData) {
        String mincode = StringUtils.isBlank(reportGradStudentData.getMincodeAtGrad()) ? reportGradStudentData.getMincode() : reportGradStudentData.getMincodeAtGrad();
        SchoolTrax traxSchool = schoolService.getTraxSchoolDetails(mincode);
        School school = new School();
        school.setStudents(new ArrayList<>());
        if(traxSchool != null) {
            school.setDistno(StringUtils.substring(traxSchool.getMinCode(), 0, 3));
            school.setMincode(traxSchool.getMinCode());
            school.setName(traxSchool.getSchoolName());
            school.setTypeBanner("Principal");
            Address address = new Address();
            address.setStreetLine1(traxSchool.getAddress1());
            address.setStreetLine2(traxSchool.getAddress2());
            address.setCity(traxSchool.getCity());
            address.setRegion(traxSchool.getProvCode());
            address.setCountry(traxSchool.getCountryName());
            address.setCode(traxSchool.getPostal());
            school.setAddress(address);
            return school;
        }
        return school;
    }

    private School populateSchoolObjectByReportGradStudentData(Map<String, School> schoolMap, ReportGradStudentData reportGradStudentData) {
        String mincode = StringUtils.isBlank(reportGradStudentData.getMincodeAtGrad()) ? reportGradStudentData.getMincode() : reportGradStudentData.getMincodeAtGrad();
        School school = schoolMap.get(mincode);
        if (school == null) {
            school = populateSchoolObjectByReportGradStudentData(reportGradStudentData);
            schoolMap.put(mincode, school);
        }
        return school;
    }

    private void processDistrictSchoolMap(List<School> schools, ReportGradStudentData reportGradStudentData) {
        boolean addNewSchool = true;
        String mincode = StringUtils.isBlank(reportGradStudentData.getMincodeAtGrad()) ? reportGradStudentData.getMincode() : reportGradStudentData.getMincodeAtGrad();
        String distNo = StringUtils.substring(mincode, 0, 3);
        for (School school : schools) {
            if (StringUtils.equals(school.getMincode(), mincode)) {
                addNewSchool = false;
                processDistrictSchool(school, reportGradStudentData);
            }
        }
        if (addNewSchool) {
            SchoolTrax schoolTrax = schoolService.getTraxSchoolDetails(mincode);
            School school = new School();
            school.setDistno(distNo);
            school.setMincode(mincode);
            school.setName(schoolTrax != null ? schoolTrax.getSchoolName() : reportGradStudentData.getSchoolName());
            school.setTypeBanner("Principal");
            schools.add(processDistrictSchool(school, reportGradStudentData));
        }
    }

    private School processDistrictSchool(School school, ReportGradStudentData reportGradStudentData) {
        String paperType = reportGradStudentData.getPaperType();
        String transcriptTypeCode = reportGradStudentData.getTranscriptTypeCode();
        String certificateTypeCode = reportGradStudentData.getCertificateTypeCode();
        logger.debug("Processing district school {} student {} for transcript {} & certificate {} and paper type {}", school.getMincode(), reportGradStudentData.getPen(), transcriptTypeCode, certificateTypeCode, paperType);
        if (StringUtils.isNotBlank(certificateTypeCode)) {
            switch (certificateTypeCode) {
                case "E", "EI", "O", "FN" -> school.getSchoolStatistic().setDogwoodCount(school.getSchoolStatistic().getDogwoodCount() + 1);
                case "A", "AI", "FNA" -> school.getSchoolStatistic().setAdultDogwoodCount(school.getSchoolStatistic().getAdultDogwoodCount() + 1);
                case "F" -> school.getSchoolStatistic().setFrenchImmersionCount(school.getSchoolStatistic().getFrenchImmersionCount() + 1);
                case "S" -> school.getSchoolStatistic().setProgramFrancophoneCount(school.getSchoolStatistic().getProgramFrancophoneCount() + 1);
                case "SC", "SCF", "SCI", "SCFN" -> school.getSchoolStatistic().setEvergreenCount(school.getSchoolStatistic().getEvergreenCount() + 1);
                default -> school.setTypeIndicator(null);
            }
            school.getSchoolStatistic().setTotalCertificateCount(
                            school.getSchoolStatistic().getDogwoodCount() +
                            school.getSchoolStatistic().getAdultDogwoodCount() +
                            school.getSchoolStatistic().getFrenchImmersionCount() +
                            school.getSchoolStatistic().getProgramFrancophoneCount() +
                            school.getSchoolStatistic().getEvergreenCount()
            );
        }
        if("YED4".equalsIgnoreCase(paperType) || StringUtils.isBlank(certificateTypeCode)) {
            school.getSchoolStatistic().setTranscriptCount(school.getSchoolStatistic().getTranscriptCount() + 1);
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
        gradStatus.setSchoolOfRecord(StringUtils.isBlank(reportGradStudentData.getMincodeAtGrad()) ? reportGradStudentData.getMincode() : reportGradStudentData.getMincodeAtGrad());
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
        String accessToken = getAccessToken();
        this.restService.post(educGraduationApiConstants.getUpdateSchoolReport(),
                requestObj,
                SchoolReports.class,
                accessToken);
    }

    private String getEncodedPdfFromBytes(byte[] bytesSAR) {
        byte[] encoded = Base64.encodeBase64(bytesSAR);
        return new String(encoded, StandardCharsets.US_ASCII);
    }

    private SchoolReports getSchoolReports(String mincode, String encodedPdf, String reportType) {
        SchoolReports requestObj = new SchoolReports();
        requestObj.setReport(encodedPdf);
        requestObj.setSchoolOfRecord(mincode);
        requestObj.setReportTypeCode(reportType);
        return requestObj;
    }

    private Pair<String, Long> getAccessToken(String accessToken) {
        return tokenUtils.getAccessToken(accessToken);
    }

    private String getAccessToken() {
        return tokenUtils.getAccessToken();
    }

    @SneakyThrows
    private byte[] mergeDocuments(List<InputStream> sources) {
        ByteArrayOutputStream tempOutStream = new ByteArrayOutputStream();
        PDFMergerUtility mergedDoc = new PDFMergerUtility();
        mergedDoc.setDestinationStream(tempOutStream);
        mergedDoc.addSources(sources);
        mergedDoc.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        return new ByteArrayInputStream(tempOutStream.toByteArray()).readAllBytes();
    }
}