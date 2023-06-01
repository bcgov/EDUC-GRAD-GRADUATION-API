package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.model.dto.GradCertificateType;
import ca.bc.gov.educ.api.graduation.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.graduation.model.dto.SchoolReports;
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
    public static final String DISTREP_SC = "DISTREP_SC";
    public static final String DISTREP_SD = "DISTREP_SD";
    public static final String ADDRESS_LABEL_YE = "ADDRESS_LABEL_YE";
    public static final String ADDRESS_LABEL_SCHL = "ADDRESS_LABEL_SCHL";

    private static final String SCHOOL_REPORTS_CREATED = "***** {} of School Reports Created *****";
    private static final String SCHOOL_DISTRICT_REPORTS_CREATED = "***** {} of School Districts Reports Created *****";
    private static final String SCHOOL_LABEL_REPORTS_CREATED = "***** {} of School Labels Reports Created *****";

    @Autowired
    WebClient webClient;
    @Autowired
    ReportService reportService;
    @Autowired
    SchoolService schoolService;
    @Autowired
    TokenUtils tokenUtils;

    @Autowired
    EducGraduationApiConstants educGraduationApiConstants;

    @Autowired
    JsonTransformer jsonTransformer;

    public byte[] getSchoolDistrictYearEndReports(String accessToken, String slrt, String drt, String srt) {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport(accessToken);
        List<InputStream> pdfs = new ArrayList<>();
        if(ADDRESS_LABEL_YE.equalsIgnoreCase(slrt)) {
            createAndStoreSchoolLabelsReports(ADDRESS_LABEL_YE, reportGradStudentDataList, accessToken, pdfs);
        }
        if(DISTREP_YE_SD.equalsIgnoreCase(drt)) {
            createAndStoreDistrictReports(DISTREP_YE_SD, reportGradStudentDataList, accessToken, pdfs);
        }
        if(DISTREP_YE_SC.equalsIgnoreCase(srt)) {
            createAndStoreSchoolReports(DISTREP_YE_SC, reportGradStudentDataList, accessToken, pdfs);
        }
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

    public byte[] getDistrictReports(String accessToken) {
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolReport(accessToken);
        List<InputStream> pdfs = new ArrayList<>();
        createAndStoreDistrictReports(DISTREP_SD, reportGradStudentDataList, accessToken, pdfs);
        return mergeDocuments(pdfs);
    }

    public Integer createAndStoreSchoolDistrictYearEndReports(String accessToken, String slrt, String drt, String srt) {
        logger.debug("***** Get Students for School Year End Reports Starts *****");
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport(accessToken);
        logger.debug("***** {} Students Retrieved *****", reportGradStudentDataList.size());
        int schoolLabelsCount = 0;
        if(ADDRESS_LABEL_YE.equalsIgnoreCase(slrt)) {
            schoolLabelsCount += createAndStoreSchoolLabelsReports(ADDRESS_LABEL_YE, reportGradStudentDataList, accessToken, null);
            logger.debug(SCHOOL_LABEL_REPORTS_CREATED, schoolLabelsCount);
        }
        int districtReportsCount = 0;
        if(DISTREP_YE_SD.equalsIgnoreCase(drt)) {
            districtReportsCount += createAndStoreDistrictReports(DISTREP_YE_SD, reportGradStudentDataList, accessToken, null);
            logger.debug(SCHOOL_DISTRICT_REPORTS_CREATED, districtReportsCount);
        }
        int schoolReportsCount = 0;
        if(DISTREP_YE_SC.equalsIgnoreCase(srt)) {
            schoolReportsCount += createAndStoreSchoolReports(DISTREP_YE_SC, reportGradStudentDataList, accessToken, null);
            logger.debug(SCHOOL_REPORTS_CREATED, schoolReportsCount);
        }
        return schoolLabelsCount + districtReportsCount + schoolReportsCount;
    }

    public Integer createAndStoreSchoolDistrictYearEndReports(String accessToken, String slrt, String drt, String srt, List<String> schools) {
        logger.debug("***** Get Students for School Year End Reports Starts *****");
        List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport(accessToken);
        logger.debug("***** {} Students Retrieved *****", reportGradStudentDataList.size());
        if(schools != null && !schools.isEmpty()) {
            boolean isDistrictSchool = schools.get(0).length() == 3;
            boolean isSchoolSchool = schools.get(0).length() > 3;
            Iterator<ReportGradStudentData> it = reportGradStudentDataList.iterator();
            while(it.hasNext()) {
                ReportGradStudentData reportGradStudentData = it.next();
                String mincode = reportGradStudentData.getMincode();
                String distcode = StringUtils.substring(mincode, 0, 3);
                if(isDistrictSchool && !schools.contains(distcode)) {
                    it.remove();
                }
                if(isSchoolSchool && !schools.contains(mincode)) {
                    it.remove();
                }
            }
        }
        int schoolLabelsCount = 0;
        if(ADDRESS_LABEL_YE.equalsIgnoreCase(slrt)) {
            schoolLabelsCount += createAndStoreSchoolLabelsReports(ADDRESS_LABEL_YE, reportGradStudentDataList, accessToken, null);
            logger.debug(SCHOOL_LABEL_REPORTS_CREATED, schoolLabelsCount);
        }
        int districtReportsCount = 0;
        if(DISTREP_YE_SD.equalsIgnoreCase(drt)) {
            districtReportsCount += createAndStoreDistrictReports(DISTREP_YE_SD, reportGradStudentDataList, accessToken, null);
            logger.debug(SCHOOL_DISTRICT_REPORTS_CREATED, districtReportsCount);
        }
        int schoolReportsCount = 0;
        if(DISTREP_YE_SC.equalsIgnoreCase(srt)) {
            schoolReportsCount += createAndStoreSchoolReports(DISTREP_YE_SC, reportGradStudentDataList, accessToken, null);
            logger.debug(SCHOOL_REPORTS_CREATED, schoolReportsCount);
        }
        return schoolLabelsCount + districtReportsCount + schoolReportsCount;
    }

    public Integer createAndStoreSchoolDistrictReports(String accessToken, List<ReportGradStudentData> reportGradStudentDataList, String slrt, String drt, String srt) {
        logger.debug("***** Get Students for School Monthly Reports Starts *****");
        int schoolLabelsCount = 0;
        if(ADDRESS_LABEL_SCHL.equalsIgnoreCase(slrt)) {
            schoolLabelsCount += createAndStoreSchoolLabelsReports(ADDRESS_LABEL_SCHL, reportGradStudentDataList, accessToken, null);
            logger.debug(SCHOOL_LABEL_REPORTS_CREATED, schoolLabelsCount);
        }
        int districtReportsCount = 0;
        if(DISTREP_SD.equalsIgnoreCase(drt)) {
            districtReportsCount += createAndStoreDistrictReports(DISTREP_SD, reportGradStudentDataList, accessToken, null);
            logger.debug(SCHOOL_DISTRICT_REPORTS_CREATED, districtReportsCount);
        }
        int schoolReportsCount = 0;
        if(DISTREP_SC.equalsIgnoreCase(srt)) {
            schoolReportsCount += createAndStoreSchoolReports(DISTREP_SC, reportGradStudentDataList, accessToken, null);
            logger.debug(SCHOOL_REPORTS_CREATED, schoolReportsCount);
        }
        return schoolLabelsCount + districtReportsCount + schoolReportsCount;
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
        } else {
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

    private Integer createAndStoreSchoolReports(String reportType, List<ReportGradStudentData> reportGradStudentDataList, String accessToken, List<InputStream> pdfs) {
        Integer reportsCount = 0;
        Map<String, School> newCredentialsSchoolMap = new HashMap<>();
        for (ReportGradStudentData reportGradStudentData : reportGradStudentDataList) {
            School school = populateSchoolObjectByReportGradStudentData(newCredentialsSchoolMap, reportGradStudentData);
            Student student = processNewCredentialsSchoolMap(reportGradStudentData);
            if (student != null) {
                school.getStudents().add(student);
            }
        }
        Map<String, School> issuedTranscriptsSchoolMap = new HashMap<>();
        for (ReportGradStudentData reportGradStudentData : reportGradStudentDataList) {
            School school = populateSchoolObjectByReportGradStudentData(issuedTranscriptsSchoolMap, reportGradStudentData);
            Student student = processIssuedTranscriptsSchoolMap(reportGradStudentData);
            if (student != null) {
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

    @SneakyThrows
    private Integer createAndStoreDistrictReports(String reportType, List<ReportGradStudentData> reportGradStudentDataList, String accessToken, List<InputStream> pdfs) {
        Integer reportsCount = 0;
        Map<School, List<School>> districtSchoolsMap = new HashMap<>();
        for (ReportGradStudentData reportGradStudentData : reportGradStudentDataList) {
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

    private void saveDistrictOrSchoolOrLabelsReport(String accessToken, String mincode, String reportType, byte[] reportAsBytes) {
        String encodedPdf = getEncodedPdfFromBytes(reportAsBytes);
        SchoolReports schoolReports = getSchoolReports(mincode, encodedPdf, reportType);
        updateSchoolReport(accessToken, schoolReports);
    }

    private byte[] getSchoolYearEndReportJasper(ReportRequest reportRequest, String accessToken) {
        logger.debug("getSchoolYearEndReportJasper(ReportRequest): {}", reportRequest);
        return webClient.post().uri(educGraduationApiConstants.getSchoolDistributionYearEnd())
                .headers(h -> {
                            h.setBearerAuth(accessToken);
                            h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                        }
                ).body(BodyInserters.fromValue(reportRequest)).retrieve().bodyToMono(byte[].class).block();
    }

    private byte[] getSchoolLabelsReportJasper(ReportRequest reportRequest, String accessToken) {
        logger.debug("getSchoolLabelsReportJasper(ReportRequest): {}", reportRequest);
        return webClient.post().uri(educGraduationApiConstants.getSchoolLabels())
                .headers(h -> {
                            h.setBearerAuth(accessToken);
                            h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                        }
                ).body(BodyInserters.fromValue(reportRequest)).retrieve().bodyToMono(byte[].class).block();
    }

    private byte[] getDistrictYearEndReportJasper(ReportRequest reportRequest, String accessToken) {
        logger.debug("getDistrictYearEndReportJasper(ReportRequest): {}", reportRequest);
        return webClient.post().uri(educGraduationApiConstants.getDistrictDistributionYearEnd())
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
        String distcode = StringUtils.substring(reportGradStudentData.getMincode(), 0, 3);
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
            district = new School();
            district.setDistno(distcode);
            district.setMincode(distcode);
            district.setName(reportGradStudentData.getDistrictName());
            districtSchoolsMap.put(district, new ArrayList<>());
        }
        return district;
    }

    private School populateSchoolObjectByReportGradStudentData(ReportGradStudentData reportGradStudentData) {
        School school = new School();
        school.setDistno(StringUtils.substring(reportGradStudentData.getMincode(), 0, 3));
        school.setMincode(reportGradStudentData.getMincode());
        school.setName(reportGradStudentData.getSchoolName());
        school.setTypeBanner("Principal");
        Address address = new Address();
        address.setStreetLine1(reportGradStudentData.getSchoolAddress1());
        address.setStreetLine2(reportGradStudentData.getSchoolAddress2());
        address.setCity(reportGradStudentData.getSchoolCity());
        address.setRegion(reportGradStudentData.getSchoolProvince());
        address.setCountry(reportGradStudentData.getSchoolCountry());
        address.setCode(reportGradStudentData.getSchoolPostal());
        school.setAddress(address);
        school.setStudents(new ArrayList<>());
        return school;
    }

    private School populateSchoolObjectByReportGradStudentData(Map<String, School> schoolMap, ReportGradStudentData reportGradStudentData) {
        String mincode = reportGradStudentData.getMincode();
        School school = schoolMap.get(mincode);
        if (school == null) {
            school = populateSchoolObjectByReportGradStudentData(reportGradStudentData);
            schoolMap.put(mincode, school);
        }
        return school;
    }

    private void processDistrictSchoolMap(List<School> schools, ReportGradStudentData reportGradStudentData) {
        boolean addNewSchool = true;
        String distNo = StringUtils.substring(reportGradStudentData.getMincode(), 0, 3);
        for (School school : schools) {
            if (StringUtils.equals(school.getMincode(), reportGradStudentData.getMincode())) {
                addNewSchool = false;
                processDistrictSchool(school, reportGradStudentData);
            }
        }
        if (addNewSchool) {
            School school = new School();
            school.setDistno(distNo);
            school.setMincode(reportGradStudentData.getMincode());
            school.setName(reportGradStudentData.getSchoolName());
            school.setTypeBanner("Principal");
            schools.add(processDistrictSchool(school, reportGradStudentData));
        }
    }

    private School processDistrictSchool(School school, ReportGradStudentData reportGradStudentData) {
        if (reportGradStudentData.getCertificateTypes() != null && !reportGradStudentData.getCertificateTypes().isEmpty()) {
            for (GradCertificateType certType : reportGradStudentData.getCertificateTypes()) {
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
        school.getSchoolStatistic().setTranscriptCount(school.getSchoolStatistic().getTranscriptCount() + 1);
        return school;
    }

    private Student processNewCredentialsSchoolMap(ReportGradStudentData reportGradStudentData) {
        if (reportGradStudentData.getCertificateTypes() != null && !reportGradStudentData.getCertificateTypes().isEmpty()) {
            return populateStudentObjectByReportGradStudentData(reportGradStudentData);
        }
        return null;
    }

    private Student processIssuedTranscriptsSchoolMap(ReportGradStudentData reportGradStudentData) {
        return populateStudentObjectByReportGradStudentData(reportGradStudentData);
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

    private void updateSchoolReport(String accessToken, SchoolReports requestObj) {
        webClient.post().uri(educGraduationApiConstants.getUpdateSchoolReport())
                .headers(h -> {
                            h.setBearerAuth(accessToken);
                            h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                        }
                ).body(BodyInserters.fromValue(requestObj)).retrieve().bodyToMono(SchoolReports.class).block();
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