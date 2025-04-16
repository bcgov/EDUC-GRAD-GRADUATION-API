package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.constants.AddressTypeCodes;
import ca.bc.gov.educ.api.graduation.constants.DistrictContactTypeCodes;
import ca.bc.gov.educ.api.graduation.constants.ReportTypeCodes;
import ca.bc.gov.educ.api.graduation.mapper.SchoolMapper;
import ca.bc.gov.educ.api.graduation.model.dto.institute.DistrictAddress;
import ca.bc.gov.educ.api.graduation.model.report.*;
import ca.bc.gov.educ.api.graduation.model.dto.institute.District;
import ca.bc.gov.educ.api.graduation.model.dto.DistrictReport;
import ca.bc.gov.educ.api.graduation.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
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

import static ca.bc.gov.educ.api.graduation.constants.ReportTypeCodes.DISTREP_YE_SD;
import static ca.bc.gov.educ.api.graduation.constants.ReportingSchoolTypesEnum.SCHOOL_AT_GRAD;
import static ca.bc.gov.educ.api.graduation.constants.ReportingSchoolTypesEnum.SCHOOL_OF_RECORD;

@Service
public class DistrictReportService extends BaseReportService {

  private static final Logger logger = LoggerFactory.getLogger(DistrictReportService.class);

  EducGraduationApiConstants educGraduationApiConstants;
  RESTService restService;
  ReportService reportService;
  SchoolService schoolService;
  DistrictService districtService;
  WebClient webClient;

  @Autowired
  public DistrictReportService(EducGraduationApiConstants educGraduationApiConstants, RESTService restService, ReportService reportService, WebClient webClient, SchoolService schoolService, DistrictService districtService) {
    this.educGraduationApiConstants = educGraduationApiConstants;
    this.restService = restService;
    this.reportService = reportService;
    this.schoolService = schoolService;
    this.districtService = districtService;
    this.webClient = webClient;
  }


  private void saveDistrictReport(UUID districtId, String reportType, byte[] reportAsBytes) {
    String encodedPdf = getEncodedPdfFromBytes(reportAsBytes);
    DistrictReport districtReport = buildDistrictReport(districtId, encodedPdf, reportType);
    updateDistrictReport(districtReport);
  }

  private DistrictReport buildDistrictReport(UUID districtId, String encodedPdf, String reportType) {
    DistrictReport districtReport = new DistrictReport();
    districtReport.setReport(encodedPdf);
    districtReport.setDistrictId(districtId);
    districtReport.setReportTypeCode(reportType);
    return districtReport;
  }

  private void updateDistrictReport(DistrictReport districtReport) {
    this.restService.post(educGraduationApiConstants.getUpdateDistrictReport(),
            districtReport,
            DistrictReport.class);
  }

  public byte[] getDistrictYearEndReports() throws IOException {
    List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport();
    List<InputStream> pdfs = new ArrayList<>();
    createAndStoreDistrictReports(DISTREP_YE_SD.getCode(), reportGradStudentDataList, pdfs);
    return mergeDocuments(pdfs);
  }

  public byte[] getDistrictYearEndNonGradReports() throws IOException {
    List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolNonGradYearEndReport();
    List<InputStream> pdfs = new ArrayList<>();
    createAndStoreDistrictReports(ReportTypeCodes.NONGRADDISTREP_SD.getCode(), reportGradStudentDataList, pdfs);
    return mergeDocuments(pdfs);
  }

  public byte[] getDistrictReports() throws IOException {
    List<ReportGradStudentData> reportGradStudentDataList = reportService.getStudentsForSchoolReport();
    List<InputStream> pdfs = new ArrayList<>();
    createAndStoreDistrictReports(ReportTypeCodes.DISTREP_SD.getCode(), reportGradStudentDataList, pdfs);
    return mergeDocuments(pdfs);
  }

  public Integer createAndStoreDistrictYearEndReports() {
    List<ReportGradStudentData> reportGradStudentDataList;
    reportGradStudentDataList = reportService.getStudentsForSchoolYearEndReport();
    return createAndStoreDistrictReports(DISTREP_YE_SD.getCode(), reportGradStudentDataList, null);
  }

  public Integer createAndStoreDistrictNonGradYearEndReport() {
    List<ReportGradStudentData> reportGradStudentDataList;
    reportGradStudentDataList = reportService.getStudentsForSchoolNonGradYearEndReport();
    return createAndStoreDistrictReports(ReportTypeCodes.NONGRADDISTREP_SD.getCode(), reportGradStudentDataList, null);
  }

  public Integer createAndStoreDistrictReportMonth() {
    List<ReportGradStudentData> reportGradStudentDataList;
    reportGradStudentDataList = reportService.getStudentsForSchoolReport();
    return createAndStoreDistrictReports(ReportTypeCodes.DISTREP_SD.getCode(), reportGradStudentDataList, null);
  }

  public Integer createAndStoreDistrictReports(String reportType, List<ReportGradStudentData> reportGradStudentDataList, List<InputStream> pdfs) {
    int reportsCount = 0;
    Map<UUID, List<School>> districtSchoolsMap = groupSchoolsByDistrict(reportGradStudentDataList);

    for (Map.Entry<UUID, List<School>> entry : districtSchoolsMap.entrySet()) {
      UUID districtId = entry.getKey();
      List<School> schools = entry.getValue();
      reportsCount += processDistrictReports(districtId, schools, reportType, pdfs);
    }
    return reportsCount;
  }

  public byte[] getDistrictLabelsReportsFromDistricts(String reportType, List<District> districts) throws IOException {
    List<InputStream> pdfs = new ArrayList<>();
    createAndStoreDistrictLabelsReportsFromDistricts(reportType, districts, pdfs);
    return mergeDocuments(pdfs);
  }

  public int createAndStoreDistrictLabelsReportsFromDistricts(String reportType, List<District> districts, List<InputStream> pdfs) {
    int reportsCount = 0;
    ReportRequest reportRequest = buildDistrictLabelsReportRequest(districts);
    byte[] reportAsBytes = getDistrictLabelsReportJasper(reportRequest);
    if (reportAsBytes != null && pdfs != null) {
      ByteArrayInputStream is = new ByteArrayInputStream(reportAsBytes);
      pdfs.add(is);
    }
    if (pdfs == null) {
      UUID districtId = (districts.size() == 1) ? UUID.fromString(districts.get(0).getDistrictId()) :UUID.fromString("00000000-0000-0000-0000-000000000000");
      saveDistrictReport(districtId, reportType, reportAsBytes);
    }
    reportsCount++;
    return reportsCount;
  }

  /**
   *  currently grad-report-api is set up such that all label reports
   *  (even for districts) take school objects as input. This is reflected
   *  here and should be updated in a future ticket.
   */
  private ReportRequest buildDistrictLabelsReportRequest(List<District> districts) {
    List<School> schools = districts.stream()
        .map(this::convertDistrictToSchool)
        .toList();

    ReportRequest reportRequest = new ReportRequest();
    reportRequest.setOptions(createReportOptions());
    reportRequest.setData(createReportData(schools));

    return reportRequest;
  }

  private School convertDistrictToSchool(District district) {
    School school = new School();
    school.setMincode(district.getDistrictNumber());
    school.setTypeBanner("SUPERINTENDENT");
    school.setName(district.getDistrictName());
    school.setAddress(getMailingAddress(district));
    return school;
  }

  private Address getMailingAddress(District district) {
    return district.getAddresses().stream()
        .filter(addr -> AddressTypeCodes.MAILING.getCode().equals(addr.getAddressTypeCode()))
        .findFirst()
        .map(this::convertToAddress)
        .orElse(new Address());
  }

  private Address convertToAddress(DistrictAddress districtAddress) {
    Address address = new Address();
    address.setStreetLine1(districtAddress.getAddressLine1());
    address.setStreetLine2(districtAddress.getAddressLine2());
    address.setCity(districtAddress.getCity());
    address.setRegion(districtAddress.getProvinceCode());
    address.setCountry(districtAddress.getCountryCode());
    address.setCode(districtAddress.getPostal());
    return address;
  }

  private ReportOptions createReportOptions() {
    ReportOptions reportOptions = new ReportOptions();
    reportOptions.setReportName("schoollabels");
    reportOptions.setReportFile("School Labels.pdf");
    return reportOptions;
  }

  private ReportData createReportData(List<School> schools) {
    ReportData data = new ReportData();
    data.setIssueDate(new Date());
    data.setSchools(schools);
    return data;
  }

  private Map<UUID, List<School>> groupSchoolsByDistrict(List<ReportGradStudentData> reportGradStudentDataList) {
    Map<UUID, List<School>> districtSchoolsMap = new HashMap<>();
    Map<String, ca.bc.gov.educ.api.graduation.model.dto.institute.School> schoolCache = new HashMap<>();

    reportGradStudentDataList.forEach(reportGradStudentData -> {
      //if not year end, reportingSchoolTypeCode isn't set, so check old way
      String schoolId = StringUtils.defaultIfBlank(reportGradStudentData.getSchoolAtGradId(), reportGradStudentData.getSchoolOfRecordId());

      if(reportGradStudentData.getReportingSchoolTypeCode() != null && reportGradStudentData.getReportingSchoolTypeCode().equals(SCHOOL_AT_GRAD.name())) {
        schoolId = reportGradStudentData.getSchoolAtGradId();
      } else if (reportGradStudentData.getReportingSchoolTypeCode() != null && reportGradStudentData.getReportingSchoolTypeCode().equals(SCHOOL_OF_RECORD.name())) {
        schoolId = reportGradStudentData.getSchoolOfRecordId();
      }

      if (StringUtils.isNotBlank(schoolId)) {
        ca.bc.gov.educ.api.graduation.model.dto.institute.School school = schoolCache.computeIfAbsent(schoolId, id -> schoolService.getSchoolById(UUID.fromString(id)));
        if (!schoolService.isIndependentSchool(school)) {
          UUID districtId = UUID.fromString(school.getDistrictId());
          School schoolReport = districtSchoolsMap.getOrDefault(districtId, new ArrayList<>())
              .stream()
              .filter(s -> s.getSchoolId().equals(school.getSchoolId()))
              .findFirst()
              .orElseGet(() -> {
                School newSchool = SchoolMapper.mapper.toSchoolReport(school);
                districtSchoolsMap.computeIfAbsent(districtId, k -> new ArrayList<>()).add(newSchool);
                return newSchool;
              });
          processDistrictSchool(schoolReport, reportGradStudentData);
        } else {
          logger.debug("Skip independent school {}", schoolId);
        }
      }
    });

    return districtSchoolsMap;
  }

  private int processDistrictReports(UUID districtId, List<School> schools, String reportType, List<InputStream> pdfs) {
    District district = districtService.getDistrictDetails(districtId);
    schools.forEach(school -> {
      school.setDistno(district.getDistrictNumber());
      school.setTypeBanner("Principal");
    });

    ReportRequest reportRequest = buildDistrictYearEndReportRequest(district);
    reportRequest.getData().getSchools().addAll(schools);

    byte[] reportAsBytes = generateReport(reportType, reportRequest);

    if (reportAsBytes != null) {
      if (pdfs != null) {
        pdfs.add(new ByteArrayInputStream(reportAsBytes));
      } else {
        saveDistrictReport(UUID.fromString(reportRequest.getData().getDistrict().getDistrictId()), reportType, reportAsBytes);
      }
      return 1;
    }

    return 0;
  }

  private byte[] generateReport(String reportType, ReportRequest reportRequest) {
    if (DISTREP_YE_SD.getCode().equalsIgnoreCase(reportType)) {
      return getDistrictYearEndReportJasper(reportRequest);
    } else {
      return getDistrictYearEndNonGradReportJasper(reportRequest);
    }
  }

  private ReportRequest buildDistrictYearEndReportRequest(District headerDistrict) {
    ReportRequest reportRequest = new ReportRequest();
    ReportOptions reportOptions = new ReportOptions();
    reportOptions.setReportName("districtdistributionyearend");
    reportOptions.setReportFile(String.format("%s District Distribution Report Year End.pdf", headerDistrict.getDistrictNumber()));
    reportRequest.setOptions(reportOptions);
    ReportData headerData = new ReportData();
    headerData.setDistrict(headerDistrict);
    headerData.setOrgCode(getReportOrgCode(headerDistrict.getDistrictNumber()));
    headerData.setIssueDate(new Date());
    headerData.setSchools(new ArrayList<>());
    reportRequest.setData(headerData);
    return reportRequest;
  }

  private void processDistrictSchool(School school, ReportGradStudentData reportGradStudentData) {
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
  }

  private String getReportOrgCode(String mincode) {
    return (StringUtils.startsWith(mincode, "098") ? "YU" : "BC");
  }

  private byte[] getDistrictYearEndReportJasper(ReportRequest reportRequest) {
    return restService.post(educGraduationApiConstants.getDistrictDistributionYearEnd(), reportRequest, byte[].class);
  }

  private byte[] getDistrictYearEndNonGradReportJasper(ReportRequest reportRequest) {
    return restService.post(educGraduationApiConstants.getDistrictDistributionYearEndNonGrad(), reportRequest, byte[].class);
  }

  private byte[] getDistrictLabelsReportJasper(ReportRequest reportRequest) {
    return restService.post(educGraduationApiConstants.getSchoolLabels(), reportRequest, byte[].class);
  }
}
