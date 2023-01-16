package ca.bc.gov.educ.api.graduation.service;

import ca.bc.gov.educ.api.graduation.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.GradProgram;
import ca.bc.gov.educ.api.graduation.model.report.GradRequirement;
import ca.bc.gov.educ.api.graduation.model.report.GraduationData;
import ca.bc.gov.educ.api.graduation.model.report.GraduationStatus;
import ca.bc.gov.educ.api.graduation.model.report.School;
import ca.bc.gov.educ.api.graduation.model.report.*;
import ca.bc.gov.educ.api.graduation.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final String GRAD_REPORT_API_DOWN = "GRAD-REPORT-API IS DOWN";
    private static final String GRAD_GRADUATION_REPORT_API_DOWN = "GRAD-GRADUATION-REPORT-API IS DOWN";
    private static final String DOCUMENT_STATUS_COMPLETED = "COMPL";

    @Autowired
    WebClient webClient;

    @Autowired
    JsonTransformer jsonTransformer;

    @Autowired
    EducGraduationApiConstants educGraduationApiConstants;

    @Autowired
    SchoolService schoolService;

    public ProgramCertificateTranscript getTranscript(GraduationStudentRecord gradResponse, ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, String accessToken, ExceptionMessage exception) {
        ProgramCertificateReq req = new ProgramCertificateReq();
        req.setProgramCode(gradResponse.getProgram());
        req.setSchoolCategoryCode(getSchoolCategoryCode(accessToken, graduationDataStatus.getGradStatus().getSchoolOfRecord()));
        try {
            return webClient.post().uri(educGraduationApiConstants.getTranscript())
                    .headers(h -> {
                        h.setBearerAuth(accessToken);
                        h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                    }).body(BodyInserters.fromValue(req)).retrieve().bodyToMono(ProgramCertificateTranscript.class).block(); 
        } catch (Exception e) {
            exception.setExceptionName(GRAD_GRADUATION_REPORT_API_DOWN);
            exception.setExceptionDetails(e.getCause() == null ? e.getLocalizedMessage() : e.getCause().getLocalizedMessage());
            return null;
        }
    }

    public List<ProgramCertificateTranscript> getCertificateList(GraduationStudentRecord gradResponse, ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, List<StudentOptionalProgram> projectedOptionalGradResponse, String accessToken, ExceptionMessage exception) {
        ProgramCertificateReq req = new ProgramCertificateReq();
        req.setProgramCode(gradResponse.getProgram());
        for (StudentOptionalProgram optionalPrograms : projectedOptionalGradResponse) {
            if (optionalPrograms.isGraduated() && (optionalPrograms.getOptionalProgramCode().equals("FI") || optionalPrograms.getOptionalProgramCode().equals("DD") || optionalPrograms.getOptionalProgramCode().equals("FR"))) {
                req.setOptionalProgram(optionalPrograms.getOptionalProgramCode());
            }
        }
        req.setSchoolCategoryCode(getSchoolCategoryCode(accessToken, graduationDataStatus.getGradStatus().getSchoolOfRecord()));
        try {
            return webClient.post().uri(educGraduationApiConstants.getCertList())
                    .headers(h -> {
                        h.setBearerAuth(accessToken);
                        h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                    }).body(BodyInserters.fromValue(req)).retrieve().bodyToMono(new ParameterizedTypeReference<List<ProgramCertificateTranscript>>() {
                    }).block(); 
        } catch (Exception e) {
            exception.setExceptionName(GRAD_GRADUATION_REPORT_API_DOWN);
            exception.setExceptionDetails(e.getCause() == null ? e.getLocalizedMessage() : e.getCause().getLocalizedMessage());
            return new ArrayList<>();
        }
    }

    public String getSchoolCategoryCode(String accessToken, String mincode) {
        CommonSchool commonSchoolObj = webClient.get().uri(String.format(educGraduationApiConstants.getSchoolCategoryCode(), mincode))
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                }).retrieve().bodyToMono(CommonSchool.class).block(); 
        if (commonSchoolObj != null) {
            return commonSchoolObj.getSchoolCategoryCode();
        }
        return null;
    }

    public ReportData prepareTranscriptData(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, GraduationStudentRecord gradResponse, boolean xml, String accessToken, ExceptionMessage exception) {
        try {
            School schoolAtGrad = getSchoolAtGradData(graduationDataStatus, accessToken, exception);
            School schoolOfRecord = getSchoolData(graduationDataStatus.getSchool());
            GraduationStatus graduationStatus = getGraduationStatus(graduationDataStatus, schoolAtGrad, schoolOfRecord);
            GraduationData graduationData = getGraduationData(graduationDataStatus);
            graduationStatus.setProgramCompletionDate(EducGraduationApiUtils.getSimpleDateFormat(graduationData.getGraduationDate()));
            ReportData data = new ReportData();
            data.setSchool(schoolOfRecord);
            data.setStudent(getStudentData(graduationDataStatus.getGradStudent()));
            data.setGradMessage(graduationStatus.getGraduationMessage());
            data.setGraduationStatus(graduationStatus);
            data.setGradProgram(getGradProgram(graduationDataStatus, accessToken));
            data.setGraduationData(graduationData);
            data.setLogo(StringUtils.startsWith(data.getSchool().getMincode(), "098") ? "YU" : "BC");
            data.setTranscript(getTranscriptData(graduationDataStatus, gradResponse, xml, accessToken, exception));
            data.setNonGradReasons(getNonGradReasons(graduationDataStatus.getNonGradReasons(), xml, accessToken));
            data.setIssueDate(EducGraduationApiUtils.formatIssueDateForReportJasper(new java.sql.Date(System.currentTimeMillis()).toString()));
            data.getStudent().setGraduationData(graduationData);
            return data;
        } catch (Exception e) {
            exception.setExceptionName("UNABLE TO GENERATE REPORT DATA");
            exception.setExceptionDetails(e.getCause() == null ? e.getLocalizedMessage() : e.getCause().getLocalizedMessage());
        }
        ReportData errorData = new ReportData();
        errorData.setException(exception);
        errorData.getParameters().put(exception.getExceptionName(), exception.getExceptionDetails());
        return errorData;
    }

    public ReportData prepareTranscriptData(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, boolean xml, String accessToken, ExceptionMessage exception) {
        try {
            String studentID = graduationDataStatus.getGradStudent().getStudentID();
            if (studentID == null) {
                throw new EntityNotFoundException(
                        ReportService.class,
                        "Student ID can't be NULL");
            }

            GraduationStudentRecord graduationStudentRecord = getGradStatusFromGradStudentApi(studentID, accessToken);
            return prepareTranscriptData(graduationDataStatus, graduationStudentRecord, xml, accessToken, exception);
        } catch (Exception e) {
            exception.setExceptionName("PREPARE REPORT DATA FROM GRADUATION STATUS");
            exception.setExceptionDetails(e.getCause() == null ? e.getLocalizedMessage() : e.getCause().getLocalizedMessage());
        }
        ReportData errorData = new ReportData();
        errorData.getParameters().put(exception.getExceptionName(), exception.getExceptionDetails());
        return errorData;
    }

    public ReportData prepareTranscriptData(String pen, boolean xml, String accessToken, ExceptionMessage exception) {
        try {
            GradSearchStudent student = getStudentByPenFromStudentApi(pen, accessToken);
            GraduationStudentRecord graduationStudentRecord = getGradStatusFromGradStudentApi(student.getStudentID(), accessToken);
            if (graduationStudentRecord.getStudentGradData() == null) {
                throw new EntityNotFoundException(
                        ReportService.class, String.format("Student with PEN %s doesn't have graduation data in GRAD Student system", pen));
            }
            ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationData = (ca.bc.gov.educ.api.graduation.model.dto.GraduationData) jsonTransformer.unmarshall(graduationStudentRecord.getStudentGradData(), ca.bc.gov.educ.api.graduation.model.dto.GraduationData.class);
            return prepareTranscriptData(graduationData, graduationStudentRecord, xml, accessToken, exception);
        } catch (Exception e) {
            exception.setExceptionName("PREPARE TRANSCRIPT REPORT DATA FROM PEN");
            exception.setExceptionDetails(e.getCause() == null ? e.getLocalizedMessage() : e.getCause().getLocalizedMessage());
        }
        ReportData errorData = new ReportData();
        errorData.getParameters().put(exception.getExceptionName(), exception.getExceptionDetails());
        return errorData;
    }

    private GradSearchStudent getStudentByPenFromStudentApi(String pen, String accessToken) {
        List<GradSearchStudent> stuDataList = webClient.get().uri(String.format(educGraduationApiConstants.getPenStudentApiByPenUrl(), pen))
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                }).retrieve().bodyToMono(new ParameterizedTypeReference<List<GradSearchStudent>>() {
                }).block(); 
        if (stuDataList != null && !stuDataList.isEmpty()) {
            return stuDataList.get(0);
        }
        throw new EntityNotFoundException(
                ReportService.class, String.format("Student with PEN %s value not exists in PEN system", pen));
    }

    private GraduationStudentRecord getGradStatusFromGradStudentApi(String studentID, String accessToken) {
        GraduationStudentRecord graduationStudentRecord = webClient.get().uri(String.format(educGraduationApiConstants.getReadGradStudentRecord(), studentID))
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                }).retrieve().bodyToMono(GraduationStudentRecord.class).block();
        if(graduationStudentRecord != null) {
            return graduationStudentRecord;
        }
        throw new EntityNotFoundException(
                ReportService.class, String.format("Student with PEN %s value not exists in GRAD Student system", studentID));
    }

    private List<NonGradReason> getNonGradReasons(List<ca.bc.gov.educ.api.graduation.model.dto.GradRequirement> nonGradReasons, boolean xml, String accessToken) {
        List<NonGradReason> nList = new ArrayList<>();
        if (nonGradReasons != null) {
            Map<String, String> traxReqCodes = new HashMap<>();
            if(xml && StringUtils.isNotBlank(accessToken)) {
                List<ProgramRequirementCode> programReqCodes = getAllProgramRequirementCodeList(accessToken);
                populateTraxReqCodesMap(programReqCodes, traxReqCodes);
            }
            for (ca.bc.gov.educ.api.graduation.model.dto.GradRequirement gR : nonGradReasons) {
                String code = xml ? traxReqCodes.get(gR.getRule()) : gR.getTranscriptRule();
                NonGradReason obj = new NonGradReason();
                obj.setCode(code);
                obj.setDescription(gR.getDescription());
                nList.add(obj);
            }
        }
        return nList;
    }

    private void populateTraxReqCodesMap(List<ProgramRequirementCode> programReqCodes, Map<String, String> traxReqCodes) {
        for(ProgramRequirementCode code: programReqCodes) {
            traxReqCodes.put(code.getProReqCode(), code.getTraxReqChar());
        }
    }

    private Transcript getTranscriptData(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, GraduationStudentRecord gradResponse, boolean xml, String accessToken, ExceptionMessage exception) {
        Transcript transcriptData = new Transcript();
        transcriptData.setInterim(xml ? "true" : "false");
        ProgramCertificateTranscript pcObj = getTranscript(gradResponse, graduationDataStatus, accessToken, exception);
        if (pcObj != null) {
            Code code = new Code();
            code.setCode(pcObj.getTranscriptTypeCode());
            transcriptData.setTranscriptTypeCode(code);
        }
        transcriptData.setIssueDate(EducGraduationApiUtils.formatIssueDateForReportJasper(new java.sql.Date(System.currentTimeMillis()).toString()));
        transcriptData.setResults(getTranscriptResults(graduationDataStatus, xml, accessToken));
        return transcriptData;
    }

    private void createCourseListForTranscript(List<StudentCourse> studentCourseList, ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, List<TranscriptResult> tList, String provincially, boolean xml) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("PST"), Locale.CANADA);
        String today = EducGraduationApiUtils.formatDate(cal.getTime(), EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
        for (StudentCourse sc : studentCourseList) {
            Date sessionDate = EducGraduationApiUtils.parseDate(sc.getSessionDate() + "/01", EducGraduationApiConstants.SECONDARY_DATE_FORMAT);
            String sDate = EducGraduationApiUtils.formatDate(sessionDate, EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
            int diff = EducGraduationApiUtils.getDifferenceInMonths(sDate, today);
            boolean notCompletedCourse = xml && diff <= 0;
            if (!sc.isDuplicate() && !sc.isFailed() && !sc.isNotCompleted() && ((notCompletedCourse) || !sc.isProjected()) && !sc.isValidationCourse()) {
                TranscriptResult result = new TranscriptResult();
                String equivOrChallenge = "";
                if (sc.getEquivOrChallenge() != null) {
                    equivOrChallenge = sc.getEquivOrChallenge();
                }
                result.setCourse(setCourseObjForTranscript(sc, graduationDataStatus));
                result.setMark(setMarkObjForTranscript(sc, graduationDataStatus.getGradStatus().getProgram(), provincially));
                result.setRequirement(sc.getGradReqMet());
                result.setUsedForGrad(sc.getCreditsUsedForGrad() != null ? sc.getCreditsUsedForGrad().toString() : "");
                result.setRequirementName(sc.getGradReqMetDetail());
                result.setEquivalency(sc.getSpecialCase() != null && sc.getSpecialCase().compareTo("C") == 0 ? "C" : equivOrChallenge);
                tList.add(result);
            }
        }
    }

    private TranscriptCourse setCourseObjForTranscript(StudentCourse sc, ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus) {
        TranscriptCourse crse = new TranscriptCourse();
        crse.setCode(sc.getCourseCode());
        crse.setCredits(getCredits(graduationDataStatus.getGradStatus().getProgram(), sc.getCourseCode(), sc.getCredits(), sc.isRestricted()));
        crse.setLevel(sc.getCourseLevel());
        crse.setName(getCourseNameLogic(sc));

        crse.setRelatedCourse(sc.getRelatedCourse());
        crse.setRelatedLevel(sc.getRelatedLevel());
        crse.setType(sc.getProvExamCourse().equals("Y") ? "1" : "2");
        crse.setSessionDate(sc.getSessionDate() != null ? sc.getSessionDate().replace("/", "") : "");
        return crse;
    }

    private Mark setMarkObjForTranscript(StudentCourse sc, String program, String provincially) {
        Mark mrk = new Mark();
        mrk.setExamPercent(getExamPercent(sc.getBestExamPercent(), program, sc.getCourseLevel(), sc.getSpecialCase(), sc.getSessionDate(), sc.getExamPercent()));
        mrk.setFinalLetterGrade(sc.getCompletedCourseLetterGrade());
        mrk.setFinalPercent(getFinalPercent(getValue(sc.getCompletedCoursePercentage()), sc.getSessionDate(), provincially));
        mrk.setInterimLetterGrade(sc.getInterimLetterGrade());
        mrk.setInterimPercent(getValue(sc.getInterimPercent()));
        mrk.setSchoolPercent(getSchoolPercent(sc.getBestSchoolPercent(), program, sc.getCourseLevel(), sc.getSessionDate(), sc.getSchoolPercent()));
        return mrk;
    }

    private String getExamPercent(Double bestExamPercent, String program, String courseLevel, String specialCase, String sDate, Double examPercent) {
        String res = checkCutOffCourseDate(sDate, examPercent);
        if (res == null) {
            String bExam = getValue(bestExamPercent);
            if (specialCase != null && specialCase.compareTo("A") == 0) {
                return "AEG";
            } else if ((program.contains("2004") || program.contains("2018")) && !courseLevel.contains("12")) {
                return "";
            } else {
                return bExam;
            }
        }
        return res;
    }

    private String checkCutOffCourseDate(String sDate, Double value) {
        String cutoffDate = "1991-11-01";
        String sessionDate = sDate + "/01";
        Date temp = EducGraduationApiUtils.parseDate(sessionDate, EducGraduationApiConstants.SECONDARY_DATE_FORMAT);
        sessionDate = EducGraduationApiUtils.formatDate(temp, EducGraduationApiConstants.DEFAULT_DATE_FORMAT);

        int diff = EducGraduationApiUtils.getDifferenceInMonths(sessionDate, cutoffDate);

        if (diff > 0) {
            return getValue(value);
        } else {
            return null;
        }
    }

    private String getSchoolPercent(Double bestSchoolPercent, String program, String courseLevel, String sDate, Double schoolPercent) {

        String res = checkCutOffCourseDate(sDate, schoolPercent);
        if (res == null) {
            String sExam = getValue(bestSchoolPercent);
            if ((program.contains("2004") || program.contains("2018")) && !courseLevel.contains("12")) {
                return "";
            } else {
                return sExam;
            }
        }
        return res;
    }

    private void createAssessmentListForTranscript(List<StudentAssessment> studentAssessmentList, ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, List<TranscriptResult> tList, boolean xml, String accessToken) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("PST"), Locale.CANADA);
        String today = EducGraduationApiUtils.formatDate(cal.getTime(), EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
        List<StudentAssessment> processList = studentAssessmentList;
        if(xml) {
            processList = removeDuplicatedAssessmentsForTranscript(studentAssessmentList);
        }
        for (StudentAssessment sc : processList) {
            boolean skipProcessing = false;
            boolean notCompletedCourse = false;
            if (sc.getSessionDate() != null) {
                Date sessionDate = EducGraduationApiUtils.parseDate(sc.getSessionDate() + "/01", EducGraduationApiConstants.SECONDARY_DATE_FORMAT);
                String sDate = EducGraduationApiUtils.formatDate(sessionDate, EducGraduationApiConstants.DEFAULT_DATE_FORMAT);
                int diff = EducGraduationApiUtils.getDifferenceInMonths(sDate, today);
                notCompletedCourse = xml && diff <= 0;
            }
            if (!sc.isDuplicate() && !sc.isFailed() && !sc.isNotCompleted() && ((notCompletedCourse) || !sc.isProjected())) {
                if ((graduationDataStatus.getGradStatus().getProgram().contains("SCCP") || graduationDataStatus.getGradStatus().getProgram().contains("1950")) && (sc.getSpecialCase().compareTo("E") == 0 || sc.getSpecialCase().compareTo("A") == 0)) {
                    skipProcessing = true;
                }
                if (!skipProcessing) {
                    TranscriptResult result = new TranscriptResult();
                    TranscriptCourse crse = new TranscriptCourse();
                    crse.setCode(sc.getAssessmentCode());
                    crse.setLevel("");
                    crse.setCredits("NA");
                    crse.setName(sc.getAssessmentName());
                    crse.setType("3");
                    crse.setSessionDate(sc.getSessionDate() != null ? sc.getSessionDate().replace("/", "") : "");
                    result.setCourse(crse);

                    Mark mrk = new Mark();

                    mrk.setExamPercent("");
                    mrk.setFinalLetterGrade("");
                    mrk.setInterimLetterGrade("");
                    mrk.setInterimPercent("");
                    mrk.setSchoolPercent("");
                    mrk.setFinalLetterGrade("NA");
                    mrk.setFinalPercent(getAssessmentFinalPercentTranscript(sc, accessToken));
                    result.setMark(mrk);
                    result.setRequirement(sc.getGradReqMet());
                    result.setRequirementName(sc.getGradReqMetDetail());
                    tList.add(result);
                }
            }
        }
    }

    public List<StudentAssessment> removeDuplicatedAssessmentsForTranscript(List<StudentAssessment> studentAssessmentList) {
        if(studentAssessmentList == null) {
            return new ArrayList<StudentAssessment>();
        }
        return studentAssessmentList.stream()
                .map(StudentAssessmentDuplicatesWrapper::new)
                .distinct()
                .map(StudentAssessmentDuplicatesWrapper::getStudentAssessment)
                .collect(Collectors.toList());
    }

    private List<TranscriptResult> getTranscriptResults(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, boolean xml, String accessToken) {
        List<TranscriptResult> tList = new ArrayList<>();
        String program = graduationDataStatus.getGradStatus().getProgram();
        List<StudentCourse> studentCourseList = graduationDataStatus.getStudentCourses().getStudentCourseList();
        if (!studentCourseList.isEmpty()) {
            if (program.contains("1950") || program.contains("1986")) {
                List<StudentCourse> provinciallyExaminable = studentCourseList.stream().filter(sc -> sc.getProvExamCourse().compareTo("Y") == 0).collect(Collectors.toList());
                if (!provinciallyExaminable.isEmpty()) {
                    sortOnCourseCode(provinciallyExaminable);
                    createCourseListForTranscript(provinciallyExaminable, graduationDataStatus, tList, "provincially", xml);
                }

                List<StudentCourse> nonExaminable = studentCourseList.stream().filter(sc -> sc.getProvExamCourse().compareTo("N") == 0).collect(Collectors.toList());
                if (!nonExaminable.isEmpty()) {
                    sortOnCourseCode(nonExaminable);
                    createCourseListForTranscript(nonExaminable, graduationDataStatus, tList, "non-examinable", xml);
                }
            } else {
                studentCourseList.sort(Comparator.comparing(StudentCourse::getCourseLevel, Comparator.nullsLast(String::compareTo)).thenComparing(StudentCourse::getCourseName));
                createCourseListForTranscript(studentCourseList, graduationDataStatus, tList, "regular", xml);
            }
        }
        List<StudentAssessment> studentAssessmentList = graduationDataStatus.getStudentAssessments().getStudentAssessmentList();
        if (studentAssessmentList == null) {
            studentAssessmentList = new ArrayList<>();
        }
        if (!studentAssessmentList.isEmpty()) {
            studentAssessmentList.sort(Comparator.comparing(StudentAssessment::getAssessmentCode));
        }

        createAssessmentListForTranscript(studentAssessmentList, graduationDataStatus, tList, xml, accessToken);
        return tList;
    }

    private void sortOnCourseCode(List<StudentCourse> cList) {
        cList.sort(Comparator.comparing(StudentCourse::getCourseCode));
    }

    private String getCredits(String program, String courseCode, Integer totalCredits, boolean isRestricted) {
        if (((program.contains("2004") || program.contains("2018")) && (courseCode.startsWith("X") || courseCode.startsWith("CP"))) || isRestricted) {
            return String.format("(%s)", totalCredits);
        }
        return String.valueOf(totalCredits);
    }

    private String getFinalPercent(String finalCompletedPercentage, String sDate, String provincially) {
        String cutoffDate = "1994-09-01";
        String sessionDate = sDate + "/01";
        if (provincially.equalsIgnoreCase("provincially")) {
            return finalCompletedPercentage;
        }
        Date temp = EducGraduationApiUtils.parseDate(sessionDate, EducGraduationApiConstants.SECONDARY_DATE_FORMAT);
        sessionDate = EducGraduationApiUtils.formatDate(temp, EducGraduationApiConstants.DEFAULT_DATE_FORMAT);

        int diff = EducGraduationApiUtils.getDifferenceInMonths(sessionDate, cutoffDate);

        if (diff >= 0) {
            return "---";
        } else {
            return finalCompletedPercentage;
        }
    }

    private String getAssessmentFinalPercentAchievement(StudentAssessment sA, String accessToken) {
        String finalPercent = getValue(sA.getProficiencyScore());
        if (sA.getSpecialCase() != null && StringUtils.isNotBlank(sA.getSpecialCase().trim())) {
            finalPercent = getSpecialCase(sA, accessToken);
        }

        if (sA.getExceededWriteFlag() != null && StringUtils.isNotBlank(sA.getExceededWriteFlag().trim()) && sA.getExceededWriteFlag().compareTo("Y") == 0) {
            finalPercent = "INV";
        }
        return finalPercent;
    }

    private String getSpecialCase(StudentAssessment sA, String accessToken) {
        String finalPercent;
        SpecialCase spC = webClient.get().uri(String.format(educGraduationApiConstants.getSpecialCase(), sA.getSpecialCase()))
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                }).retrieve().bodyToMono(SpecialCase.class).block();
        finalPercent = spC != null ? spC.getLabel() : "";
        return finalPercent;
    }

    private String getAssessmentFinalPercentTranscript(StudentAssessment sA, String accessToken) {
        String finalPercent = getValue(sA.getProficiencyScore());
        if ((sA.getAssessmentCode().equalsIgnoreCase("LTE10") || sA.getAssessmentCode().equalsIgnoreCase("LTP10")) && (sA.getSpecialCase() == null || StringUtils.isBlank(sA.getSpecialCase().trim())) && StringUtils.isNotBlank(finalPercent)) {
            finalPercent = "RM";
        }
        if (sA.getSpecialCase() != null && StringUtils.isNotBlank(sA.getSpecialCase().trim()) && !sA.getSpecialCase().equalsIgnoreCase("X") && !sA.getSpecialCase().equalsIgnoreCase("Q")) {
            finalPercent = getSpecialCase(sA, accessToken);
        }
        return finalPercent;
    }

    private String getCourseNameLogic(StudentCourse sc) {
        if (sc.getGenericCourseType() != null && sc.getGenericCourseType().equalsIgnoreCase("I") && StringUtils.isNotBlank(sc.getRelatedCourse()) && StringUtils.isNotBlank(sc.getRelatedLevel()) && StringUtils.isNotBlank(sc.getRelatedCourseName())) {
            return "IDS " + sc.getRelatedCourseName();
        }
        if (StringUtils.isNotBlank(sc.getCustomizedCourseName())) {
            return sc.getCustomizedCourseName();
        }
        return sc.getCourseName();
    }

    private String getValue(Double value) {
        return value != null && value != 0.0 ? new DecimalFormat("#").format(value) : "";
    }

    private ca.bc.gov.educ.api.graduation.model.report.GraduationData getGraduationData(
            ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus) {
        GraduationData data = new GraduationData();
        data.setDogwoodFlag(graduationDataStatus.isDualDogwood());
        if (graduationDataStatus.isGraduated()) {
            if (!graduationDataStatus.getGradStatus().getProgram().equalsIgnoreCase("SCCP")) {
                if (graduationDataStatus.getGradStatus().getProgramCompletionDate() != null) {
                    if (graduationDataStatus.getGradStatus().getProgramCompletionDate().length() > 7) {
                        data.setGraduationDate(EducGraduationApiUtils.formatIssueDateForReportJasper(graduationDataStatus.getGradStatus().getProgramCompletionDate()));
                    } else {
                        data.setGraduationDate(EducGraduationApiUtils.formatIssueDateForReportJasper(EducGraduationApiUtils.parsingNFormating(graduationDataStatus.getGradStatus().getProgramCompletionDate())));
                    }
                }
                data.setHonorsFlag(graduationDataStatus.getGradStatus().getHonoursStanding().equals("Y"));
            } else {
                data.setGraduationDate(EducGraduationApiUtils.formatIssueDateForReportJasper(EducGraduationApiUtils.parsingNFormating(graduationDataStatus.getGradStatus().getProgramCompletionDate())));
            }
        }
        List<GradAlgorithmOptionalStudentProgram> optionalGradStatus = graduationDataStatus.getOptionalGradStatus();
        if(optionalGradStatus != null) {
            optionalGradStatus.removeIf(p -> "FR".equalsIgnoreCase(p.getOptionalProgramCode()));
            /***
            for (GradAlgorithmOptionalStudentProgram op : optionalGradStatus) {
                String code = op.getOptionalProgramCode();
                if(!StringUtils.isBlank(code)) {
                    switch (code) {
                        case "FI":
                            data.getProgramCodes().add("FIP");
                            break;
                        case "DD":
                            data.getProgramCodes().add("PFD");
                            break;
                        default:
                            data.getProgramCodes().add(code);
                            break;
                    }
                }
            }
            **/
        }
        return data;
    }

    private GradProgram getGradProgram(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, String accessToken) {
        GradProgram gPgm = new GradProgram();
        Code code = new Code();
        if (graduationDataStatus.getGradStatus().getProgram() != null) {
            ca.bc.gov.educ.api.graduation.model.dto.GradProgram gradProgram = webClient.get().uri(String.format(educGraduationApiConstants.getProgramNameEndpoint(), graduationDataStatus.getGradStatus().getProgram()))
                    .headers(h -> {
                        h.setBearerAuth(accessToken);
                        h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                    }).retrieve().bodyToMono(ca.bc.gov.educ.api.graduation.model.dto.GradProgram.class).block(); 
            if (gradProgram != null) {
                code.setDescription(gradProgram.getProgramName());
                code.setName(gradProgram.getProgramName());
            }
        }
        code.setCode(graduationDataStatus.getGradStatus().getProgram());
        gPgm.setCode(code);
        return gPgm;
    }

    private List<ProgramRequirementCode> getAllProgramRequirementCodeList(String accessToken) {
        final ParameterizedTypeReference<List<ProgramRequirementCode>> responseType = new ParameterizedTypeReference<>() {
        };
        return this.webClient.get()
                .uri(educGraduationApiConstants.getProgramRequirementsEndpoint())
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve().bodyToMono(responseType).block();
    }

    private Student getStudentData(GradSearchStudent gradStudent) {
        Student std = new Student();
        std.setBirthdate(EducGraduationApiUtils.parseDate(gradStudent.getDob()));
        std.setGrade(gradStudent.getStudentGrade());
        std.setStudStatus(gradStudent.getStudentStatus());
        std.setFirstName(gradStudent.getLegalFirstName());
        std.setMiddleName(gradStudent.getLegalMiddleNames());
        std.setLastName(gradStudent.getLegalLastName());
        std.setGender(gradStudent.getGenderCode());
        std.setCitizenship(gradStudent.getStudentCitizenship());
        Pen pen = new Pen();
        pen.setPen(gradStudent.getPen());
        pen.setEntityID(gradStudent.getStudentID());
        std.setPen(pen);
        return std;
    }

    private School getSchoolAtGradData(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, String accessToken, ExceptionMessage exception) {
        if (graduationDataStatus.getGradStatus() != null && !StringUtils.isBlank(graduationDataStatus.getGradStatus().getSchoolAtGrad())) {
            SchoolTrax schoolDetails = schoolService.getSchoolDetails(graduationDataStatus.getGradStatus().getSchoolAtGrad(), accessToken, exception);
            if (schoolDetails != null) {
                return getSchoolData(schoolDetails);
            }
        }
        return null;
    }

    @SuppressWarnings("DuplicatedCode")
    private School getSchoolData(SchoolTrax schoolDetails) {
        School schObj = new School();
        Address addRess = new Address();
        addRess.setCity(schoolDetails.getCity());
        addRess.setCode(schoolDetails.getPostal());
        addRess.setCountry(schoolDetails.getCountryCode());
        addRess.setRegion(schoolDetails.getProvCode());
        addRess.setStreetLine1(schoolDetails.getAddress1());
        addRess.setStreetLine2(schoolDetails.getAddress2());
        schObj.setTypeIndicator(schoolDetails.getIndependentDesignation());
        schObj.setAddress(addRess);
        schObj.setMincode(schoolDetails.getMinCode());
        schObj.setName(schoolDetails.getSchoolName());
        schObj.setSignatureCode(schoolDetails.getMinCode().substring(0, 3));
        schObj.setDistno(schoolDetails.getMinCode().substring(0, 3));
        schObj.setSchlno(schoolDetails.getMinCode());
        schObj.setStudents(new ArrayList<>());
        return schObj;
    }

    @SuppressWarnings("DuplicatedCode")
    private School getSchoolData(ca.bc.gov.educ.api.graduation.model.dto.School school) {
        School schObj = new School();
        Address addRess = new Address();
        addRess.setCity(school.getCity());
        addRess.setCode(school.getPostal());
        addRess.setCountry(school.getCountryCode());
        addRess.setRegion(school.getProvCode());
        addRess.setStreetLine1(school.getAddress1());
        addRess.setStreetLine2(school.getAddress2());
        schObj.setTypeIndicator(school.getIndependentDesignation());
        schObj.setAddress(addRess);
        schObj.setMincode(school.getMinCode());
        schObj.setName(school.getSchoolName());
        schObj.setSignatureCode(school.getMinCode().substring(0, 3));
        schObj.setDistno(school.getMinCode().substring(0, 3));
        schObj.setSchlno(school.getMinCode());
        schObj.setStudents(new ArrayList<>());
        return schObj;
    }

    private GraduationStatus getGraduationStatus(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationData, School schoolAtGrad, School schoolOfRecord) {
        GraduationStatus gradStatus = new GraduationStatus();
        String gradMessage = graduationData.getGradMessage();
        if(schoolAtGrad != null
                && schoolOfRecord != null
                && !StringUtils.equalsIgnoreCase(schoolOfRecord.getMincode(), schoolAtGrad.getMincode())) {
            gradMessage = StringUtils.replace(gradMessage, schoolOfRecord.getName(), schoolAtGrad.getName());
        }
        gradStatus.setGraduationMessage(gradMessage);
        return gradStatus;
    }

    private Student getStudentDataAchvReport(GradSearchStudent studentObj, List<StudentOptionalProgram> optionalStudentProgram) {
        Student studObj = new Student();
        studObj.setGender(StudentGenderEnum.valueOf(studentObj.getGenderCode()).toString());
        studObj.setCitizenship(studentObj.getStudentCitizenship());
        studObj.setFirstName(studentObj.getLegalFirstName());
        studObj.setLastName(studentObj.getLegalLastName());
        studObj.setGrade(studentObj.getStudentGrade());
        Pen pen = new Pen();
        pen.setPen(studentObj.getPen());
        studObj.setPen(pen);
        studObj.setLocalId(studentObj.getLocalID());
        studObj.setGradProgram(studentObj.getProgram());
        studObj.setBirthdate(EducGraduationApiUtils.formatIssueDateForReportJasper(studentObj.getDob()));
        List<OtherProgram> otherProgramParticipation = new ArrayList<>();
        for (StudentOptionalProgram sp : optionalStudentProgram) {
            OtherProgram op = new OtherProgram();
            op.setProgramCode(sp.getOptionalProgramCode());
            op.setProgramName(sp.getOptionalProgramName());
            otherProgramParticipation.add(op);
        }
        if (!otherProgramParticipation.isEmpty()) {
            studObj.setOtherProgramParticipation(otherProgramParticipation);
            studObj.setHasOtherProgram("Other Program Participation");
        } else {
            studObj.setHasOtherProgram(" ");
        }
        return studObj;
    }

    private void getStudentCoursesAssessmentsNExams(ReportData data, ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, String accessToken) {
        List<StudentCourse> sCList = graduationDataStatus.getStudentCourses().getStudentCourseList();
        List<StudentCourse> studentExamList = sCList
                .stream()
                .filter(sc -> "Y".compareTo(sc.getProvExamCourse()) == 0)
                .collect(Collectors.toList());
        List<StudentCourse> studentCourseList = sCList
                .stream()
                .filter(sc -> "N".compareTo(sc.getProvExamCourse()) == 0)
                .collect(Collectors.toList());
        List<StudentAssessment> studentAssessmentList = graduationDataStatus.getStudentAssessments().getStudentAssessmentList();
        List<AchievementCourse> sCourseList = new ArrayList<>();
        List<Exam> sExamList = new ArrayList<>();
        data.setStudentCourses(processStudentCourses(sCourseList, studentCourseList));
        Assessment achv = new Assessment();
        achv.setIssueDate(EducGraduationApiUtils.formatIssueDateForReportJasper(EducGraduationApiUtils.getSimpleDateFormat(new Date())));
        achv.setResults(getAssessmentResults(studentAssessmentList, graduationDataStatus.getGradProgram(), accessToken));
        data.setAssessment(achv);
        data.setStudentExams(processStudentExams(sExamList, studentExamList));
    }

    private List<Exam> processStudentExams(List<Exam> sExamList, List<StudentCourse> studentExamList) {
        for (StudentCourse sc : studentExamList) {
            Exam crse = new Exam();
            String equivOrChallenge = "";
            if (sc.getEquivOrChallenge() != null) {
                equivOrChallenge = sc.getEquivOrChallenge();
            }
            crse.setCourseCode(sc.getCourseCode());
            crse.setCredits(sc.getCredits().toString());
            crse.setCourseLevel(sc.getCourseLevel());
            crse.setCourseName(getCourseNameLogic(sc));
            crse.setSessionDate(sc.getSessionDate() != null ? sc.getSessionDate() : "");
            crse.setCompletedCourseLetterGrade(sc.getCompletedCourseLetterGrade());
            crse.setCompletedCoursePercentage(getValue(sc.getCompletedCoursePercentage()));
            crse.setGradReqMet(sc.getGradReqMet());
            crse.setProjected(sc.isProjected());
            crse.setCreditsUsedForGrad(sc.getCreditsUsedForGrad() != null ? sc.getCreditsUsedForGrad() : 0);
            crse.setEquivOrChallenge(equivOrChallenge);
            crse.setBestSchoolPercent(getValue(sc.getBestSchoolPercent()));
            crse.setBestExamPercent(getValue(sc.getBestExamPercent()));
            crse.setMetLitNumRequirement(sc.getMetLitNumRequirement() != null ? sc.getMetLitNumRequirement() : "");
            sExamList.add(crse);
        }

        if (!sExamList.isEmpty()) {
            sExamList.sort(Comparator.comparing(Exam::getCourseCode)
                    .thenComparing(Exam::getCourseLevel)
                    .thenComparing(Exam::getSessionDate));
        }
        return sExamList;
    }

    private List<AchievementCourse> processStudentCourses(List<AchievementCourse> sCourseList, List<StudentCourse> studentCourseList) {
        for (StudentCourse sc : studentCourseList) {
            AchievementCourse crse = new AchievementCourse();
            String equivOrChallenge = "";
            if (sc.getEquivOrChallenge() != null) {
                equivOrChallenge = sc.getEquivOrChallenge();
            }

            crse.setCourseCode(sc.getCourseCode());
            crse.setCredits(sc.getCredits().toString());
            crse.setCourseLevel(sc.getCourseLevel());
            crse.setCourseName(getCourseNameLogic(sc));
            crse.setProjected(sc.isProjected());
            crse.setInterimPercent(getValue(sc.getInterimPercent()));
            crse.setSessionDate(sc.getSessionDate() != null ? sc.getSessionDate() : "");
            crse.setCompletedCourseLetterGrade(sc.getCompletedCourseLetterGrade());
            crse.setCompletedCoursePercentage(getValue(sc.getCompletedCoursePercentage()));
            crse.setGradReqMet(sc.getGradReqMet());
            crse.setUsedForGrad(sc.getCreditsUsedForGrad() != null ? sc.getCreditsUsedForGrad().toString() : "0");
            crse.setEquivOrChallenge(equivOrChallenge);
            sCourseList.add(crse);
        }
        if (!sCourseList.isEmpty()) {
            sCourseList.sort(Comparator.comparing(AchievementCourse::getCourseCode)
                    .thenComparing(AchievementCourse::getCourseLevel)
                    .thenComparing(AchievementCourse::getSessionDate));
        }
        return sCourseList;
    }

    private List<AssessmentResult> getAssessmentResults(List<StudentAssessment> studentAssessmentList, GraduationProgramCode graduationProgramCode, String accessToken) {
        List<AssessmentResult> tList = new ArrayList<>();
        for (StudentAssessment sA : studentAssessmentList) {
            AssessmentResult result = new AssessmentResult();
            result.setAssessmentCode(sA.getAssessmentCode());
            result.setAssessmentName(sA.getAssessmentName());
            result.setGradReqMet(sA.getGradReqMet());
            result.setSessionDate(sA.getSessionDate() != null ? sA.getSessionDate() : "");
            result.setProficiencyScore(getAssessmentFinalPercentAchievement(sA, accessToken));
            result.setSpecialCase(sA.getSpecialCase());
            result.setExceededWriteFlag(sA.getExceededWriteFlag());
            result.setProjected(sA.isProjected());
            tList.add(result);
        }
        if (!tList.isEmpty()) {
            tList.removeIf(a->"A".equalsIgnoreCase(a.getSpecialCase()) && (graduationProgramCode.getProgramCode().contains("SCCP") || graduationProgramCode.getProgramCode().contains("1950")));
            tList.sort(Comparator.comparing(AssessmentResult::getAssessmentCode)
                    .thenComparing(AssessmentResult::getSessionDate));
        }
        return tList;
    }

    public void saveStudentTranscriptReportJasper(ReportData sample, String accessToken, UUID studentID, ExceptionMessage exception, boolean isGraduated, boolean overwrite) {

        String encodedPdfReportTranscript = generateStudentTranscriptReportJasper(sample, accessToken, exception);
        GradStudentTranscripts requestObj = new GradStudentTranscripts();
        requestObj.setTranscript(encodedPdfReportTranscript);
        requestObj.setStudentID(studentID);
        requestObj.setTranscriptTypeCode(sample.getTranscript().getTranscriptTypeCode().getCode());
        requestObj.setDocumentStatusCode("IP");
        requestObj.setOverwrite(overwrite);
        if (isGraduated)
            requestObj.setDocumentStatusCode(DOCUMENT_STATUS_COMPLETED);

        try {
            webClient.post().uri(String.format(educGraduationApiConstants.getUpdateGradStudentTranscript(), isGraduated))
                    .headers(h -> {
                        h.setBearerAuth(accessToken);
                        h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                    }).body(BodyInserters.fromValue(requestObj)).retrieve().bodyToMono(GradStudentReports.class).block(); 
        } catch (Exception e) {
            if (exception.getExceptionName() == null) {
                exception.setExceptionName(GRAD_GRADUATION_REPORT_API_DOWN);
                exception.setExceptionDetails(e.getLocalizedMessage());
            }
        }

    }

    private String generateStudentTranscriptReportJasper(ReportData sample,
                                                         String accessToken, ExceptionMessage exception) {
        ReportOptions options = new ReportOptions();
        options.setReportFile("transcript");
        options.setReportName("Transcript Report.pdf");
        ReportRequest reportParams = new ReportRequest();
        reportParams.setOptions(options);
        reportParams.setData(sample);
        try {
            byte[] bytesSAR = webClient.post().uri(educGraduationApiConstants.getTranscriptReport())
                    .headers(h -> {
                        h.setBearerAuth(accessToken);
                        h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                    }).body(BodyInserters.fromValue(reportParams)).retrieve().bodyToMono(byte[].class).block(); 
            return getEncodedStringFromBytes(bytesSAR);
        } catch (Exception e) {
            exception.setExceptionName(GRAD_REPORT_API_DOWN);
            exception.setExceptionDetails(e.getLocalizedMessage());
            return null;
        }
    }

    private String getEncodedStringFromBytes(byte[] bytesSAR) {
        byte[] encoded = Base64.encodeBase64(bytesSAR);
        return new String(encoded, StandardCharsets.US_ASCII);
    }

    public ReportData prepareCertificateData(String pen, String accessToken, ExceptionMessage exception) {
        try {
            GradSearchStudent student = getStudentByPenFromStudentApi(pen, accessToken);
            GraduationStudentRecord graduationStudentRecord = getGradStatusFromGradStudentApi(student.getStudentID(), accessToken);
            if (graduationStudentRecord.getStudentGradData() == null) {
                throw new EntityNotFoundException(
                        ReportService.class,
                        String.format("Student with PEN %s doesn't have graduation data in GRAD Student system", pen));
            }
            ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationData = (ca.bc.gov.educ.api.graduation.model.dto.GraduationData) jsonTransformer.unmarshall(graduationStudentRecord.getStudentGradData(), ca.bc.gov.educ.api.graduation.model.dto.GraduationData.class);
            return prepareCertificateData(graduationStudentRecord, graduationData, accessToken);
        } catch (Exception e) {
            exception.setExceptionName("PREPARE CERTIFICATE REPORT DATA FROM PEN");
            exception.setExceptionDetails(e.getCause() == null ? e.getLocalizedMessage() : e.getCause().getLocalizedMessage());
        }
        ReportData errorData = new ReportData();
        errorData.getParameters().put(exception.getExceptionName(), exception.getExceptionDetails());
        return errorData;
    }

    public ReportData prepareCertificateData(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, String accessToken, ExceptionMessage exception) {
        try {
            String studentID = graduationDataStatus.getGradStudent().getStudentID();
            if (studentID == null) {
                throw new EntityNotFoundException(
                        ReportService.class,
                        "Student ID can't be NULL");
            }

            GraduationStudentRecord graduationStudentRecord = getGradStatusFromGradStudentApi(studentID, accessToken);
            return prepareCertificateData(graduationStudentRecord, graduationDataStatus, accessToken);
        } catch (Exception e) {
            exception.setExceptionName("PREPARE REPORT DATA FROM GRADUATION STATUS");
            exception.setExceptionDetails(e.getCause() == null ? e.getLocalizedMessage() : e.getCause().getLocalizedMessage());
        }
        ReportData errorData = new ReportData();
        errorData.getParameters().put(exception.getExceptionName(), exception.getExceptionDetails());
        return errorData;
    }

    public ReportData prepareCertificateData(GraduationStudentRecord gradResponse,
                                             ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, String accessToken) {
        ExceptionMessage exceptionMessage = new ExceptionMessage();
        ProgramCertificateTranscript certType = getTranscript(gradResponse, graduationDataStatus, accessToken, exceptionMessage);
        if (StringUtils.trimToNull(exceptionMessage.getExceptionName()) != null) {
            ReportData errorData = new ReportData();
            errorData.getParameters().put(exceptionMessage.getExceptionName(), exceptionMessage.getExceptionDetails());
            return errorData;
        }
        return prepareCertificateData(gradResponse, graduationDataStatus, certType, accessToken);
    }

    public ReportData prepareCertificateData(GraduationStudentRecord gradResponse,
                                             ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, ProgramCertificateTranscript certType, String accessToken) {
        ReportData data = new ReportData();
        GraduationData graduationData = getGraduationData(graduationDataStatus);
        data.setSchool(getSchoolData(graduationDataStatus.getSchool()));
        data.setStudent(getStudentData(graduationDataStatus.getGradStudent()));
        data.setGradProgram(getGradProgram(graduationDataStatus, accessToken));
        data.setGraduationData(graduationData);
        data.setUpdateDate(EducGraduationApiUtils.formatDateForReportJasper(gradResponse.getUpdateDate().toString()));
        data.setCertificate(getCertificateData(gradResponse, certType));
        data.getStudent().setGraduationData(graduationData);
        switch (certType.getCertificateTypeCode()) {
            case "F":
            case "SCF":
            case "S":
                data.getStudent().setFrenchCert(certType.getCertificateTypeCode());
                break;
            default:
                data.getStudent().setEnglishCert(certType.getCertificateTypeCode());
        }
        return data;
    }

    public void saveStudentCertificateReportJasper(GraduationStudentRecord gradResponse, ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, String accessToken,
                                                   ProgramCertificateTranscript certType) {
        ReportData certData = prepareCertificateData(gradResponse, graduationDataStatus, certType, accessToken);
        String encodedPdfReportCertificate = generateStudentCertificateReportJasper(certData, accessToken);
        GradStudentCertificates requestObj = new GradStudentCertificates();
        requestObj.setPen(gradResponse.getPen());
        requestObj.setStudentID(gradResponse.getStudentID());
        requestObj.setCertificate(encodedPdfReportCertificate);
        requestObj.setGradCertificateTypeCode(certType.getCertificateTypeCode());
        requestObj.setDocumentStatusCode(DOCUMENT_STATUS_COMPLETED);
        webClient.post().uri(educGraduationApiConstants.getUpdateGradStudentCertificate())
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                }).body(BodyInserters.fromValue(requestObj)).retrieve().bodyToMono(GradStudentCertificates.class).block(); 

    }

    private Certificate getCertificateData(GraduationStudentRecord gradResponse, ProgramCertificateTranscript certData) {
        Certificate cert = new Certificate();
        cert.setIssued(EducGraduationApiUtils.formatIssueDateForReportJasper(EducGraduationApiUtils.parsingDateForCertificate(gradResponse.getProgramCompletionDate())));
        OrderType orTy = new OrderType();
        orTy.setName("Certificate");
        CertificateType certType = new CertificateType();
        PaperType pType = new PaperType();
        pType.setCode(certData.getCertificatePaperType());
        certType.setPaperType(pType);
        certType.setReportName(certData.getCertificateTypeCode());
        orTy.setCertificateType(certType);
        cert.setOrderType(orTy);
        cert.setCertStyle("Original");
        return cert;
    }

    private String generateStudentCertificateReportJasper(ReportData sample, String accessToken) {
        ReportOptions options = new ReportOptions();
        options.setReportFile("certificate");
        options.setReportName("Certificate.pdf");
        ReportRequest reportParams = new ReportRequest();
        reportParams.setOptions(options);
        reportParams.setData(sample);
        byte[] bytesSAR = webClient.post().uri(educGraduationApiConstants.getCertificateReport())
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                }).body(BodyInserters.fromValue(reportParams)).retrieve().bodyToMono(byte[].class).block();
        return getEncodedStringFromBytes(bytesSAR);
    }

    private String generateStudentAchievementReportJasper(ReportData data, String accessToken) {
        ReportOptions options = new ReportOptions();
        options.setReportFile("achievement");
        options.setReportName("Student Achievement Report.pdf");
        ReportRequest reportParams = new ReportRequest();
        reportParams.setOptions(options);
        reportParams.setData(data);
        byte[] bytesSAR = webClient.post().uri(educGraduationApiConstants.getAchievementReport())
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                }).body(BodyInserters.fromValue(reportParams)).retrieve().bodyToMono(byte[].class).block();
        return getEncodedStringFromBytes(bytesSAR);
    }

    public ReportData prepareAchievementReportData(ca.bc.gov.educ.api.graduation.model.dto.GraduationData graduationDataStatus, List<StudentOptionalProgram> optionalProgramList, String accessToken, ExceptionMessage exception) {
        try {
            School schoolAtGrad = getSchoolAtGradData(graduationDataStatus, accessToken, exception);
            School schoolOfRecord = getSchoolData(graduationDataStatus.getSchool());
            ReportData data = new ReportData();
            data.setSchool(schoolOfRecord);
            data.setStudent(getStudentDataAchvReport(graduationDataStatus.getGradStudent(), optionalProgramList));
            data.setOrgCode(StringUtils.startsWith(data.getSchool().getMincode(), "098") ? "YU" : "BC");
            data.setGraduationStatus(getGraduationStatus(graduationDataStatus, schoolAtGrad, schoolOfRecord));
            data.setGradProgram(getGradProgram(graduationDataStatus, accessToken));
            getStudentCoursesAssessmentsNExams(data, graduationDataStatus, accessToken);
            data.setNonGradReasons(getNonGradReasons(graduationDataStatus.getNonGradReasons(), false, null));
            data.setOptionalPrograms(getOptionalProgramAchvReport(optionalProgramList));
            data.setIssueDate(EducGraduationApiUtils.formatIssueDateForReportJasper(new java.sql.Date(System.currentTimeMillis()).toString()));
            return data;
        } catch (Exception e) {
            exception.setExceptionName("PREPARING REPORTING DATA IS DOWN");
            exception.setExceptionDetails(e.getLocalizedMessage());
            ReportData dR = new ReportData();
            dR.setException(exception);
            return dR;
        }
    }

    private List<OptionalProgram> getOptionalProgramAchvReport(List<StudentOptionalProgram> optionalProgramList) {
        List<OptionalProgram> opList = new ArrayList<>();
        for (StudentOptionalProgram sPO : optionalProgramList) {
            OptionalProgram op = new OptionalProgram();
            op.setOptionalProgramCode(sPO.getOptionalProgramCode());
            op.setOptionalProgramName(sPO.getOptionalProgramName());
            op.setProgramCompletionDate(sPO.getOptionalProgramCompletionDate());

            GradAlgorithmOptionalStudentProgram existingData = null;
            try {
                existingData = new ObjectMapper().readValue(sPO.getStudentOptionalProgramData(), GradAlgorithmOptionalStudentProgram.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            if (existingData != null && existingData.getOptionalNonGradReasons() != null) {
                op.setNonGradReasons(getNonGradReasons(existingData.getOptionalNonGradReasons(), false, null));
            }
            op.setHasRequirementMet(" Check with School");
            if (existingData != null && existingData.getOptionalRequirementsMet() != null) {
                op.setHasRequirementMet("The Following Requirements Are Met");
                op.setRequirementMet(getRequirementsMetAchvReport(existingData.getOptionalRequirementsMet(), existingData.getOptionalStudentCourses(), op.getNonGradReasons()));
            }
            opList.add(op);
        }
        return opList;
    }

    private List<GradRequirement> getRequirementsMetAchvReport(List<ca.bc.gov.educ.api.graduation.model.dto.GradRequirement> optionalRequirementsMet, StudentCourses optionalStudentCourses, List<NonGradReason> nonGradReasons) {
        List<GradRequirement> grList = new ArrayList<>();
        for (ca.bc.gov.educ.api.graduation.model.dto.GradRequirement gr : optionalRequirementsMet) {
            if (!gr.isProjected()) {
                GradRequirement gRAchv = new GradRequirement();
                gRAchv.setCode(gr.getTranscriptRule());
                gRAchv.setDescription(gr.getDescription());

                List<StudentCourse> scList = optionalStudentCourses.getStudentCourseList()
                        .stream()
                        .filter(sc -> gr.getTranscriptRule() != null && sc.getGradReqMet().contains(gr.getTranscriptRule()))
                        .collect(Collectors.toList());
                List<AchievementCourse> cdList = new ArrayList<>();
                scList.forEach(sc -> {
                    AchievementCourse cD = new AchievementCourse();
                    cD.setCourseCode(sc.getCourseCode());
                    cD.setCourseLevel(sc.getCourseLevel());
                    cD.setSessionDate(sc.getSessionDate());
                    cdList.add(cD);
                });

                gRAchv.setCourseDetails(cdList);
                grList.add(gRAchv);
            } else {
                NonGradReason obj = new NonGradReason();
                obj.setCode(gr.getTranscriptRule());
                obj.setDescription(gr.getDescription());
                nonGradReasons.add(obj);
            }
        }
        return grList;
    }

    public ExceptionMessage saveStudentAchivementReportJasper(String pen, ReportData sample, String accessToken, UUID studentID, ExceptionMessage exception, boolean isGraduated) {
        String encodedPdfReportTranscript = generateStudentAchievementReportJasper(sample, accessToken);
        GradStudentReports requestObj = new GradStudentReports();
        requestObj.setPen(pen);
        requestObj.setReport(encodedPdfReportTranscript);
        requestObj.setStudentID(studentID);
        requestObj.setGradReportTypeCode("ACHV");
        requestObj.setDocumentStatusCode("IP");
        if (isGraduated)
            requestObj.setDocumentStatusCode(DOCUMENT_STATUS_COMPLETED);

        webClient.post().uri(String.format(educGraduationApiConstants.getUpdateGradStudentReport(), isGraduated))
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGraduationApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                }).body(BodyInserters.fromValue(requestObj)).retrieve().bodyToMono(GradStudentReports.class).block();

        return exception;

    }
}
