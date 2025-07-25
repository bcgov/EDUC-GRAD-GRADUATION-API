package ca.bc.gov.educ.api.graduation.process;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.service.ReportService;
import ca.bc.gov.educ.api.graduation.util.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AlgorithmSupport {

    @Autowired
    ReportService reportService;

    @Autowired
    TokenUtils tokenUtils;

    public boolean checkForErrors(GraduationData graduationDataStatus, AlgorithmResponse algorithmResponse, ProcessorData processorData) {
        if(graduationDataStatus != null) {
            if (graduationDataStatus.getException() != null && graduationDataStatus.getException().getExceptionName() != null) {
                log.debug("**** Grad Algorithm Has Errors: ****");
                algorithmResponse.setException(graduationDataStatus.getException());
                processorData.setAlgorithmResponse(algorithmResponse);
                return true;
            }
        }else {
            log.debug("**** Grad Algorithm Has Errors: ****");
            ExceptionMessage exceptionMessage = new ExceptionMessage();
            exceptionMessage.setExceptionName("GRAD-ALGORITHM-API FAILED");
            algorithmResponse.setException(exceptionMessage);
            processorData.setAlgorithmResponse(algorithmResponse);
            return true;
        }
        return false;
    }

    public ExceptionMessage createStudentCertificateTranscriptReports(GraduationData graduationDataStatus, GraduationStudentRecord graduationStatusResponse, GraduationStudentRecord gradResponse, List<StudentOptionalProgram> projectedOptionalGradResponse, ExceptionMessage exception, ReportData data, ProcessorData processorData, String processName) {
        if(graduationDataStatus != null) {
            try {
                if (graduationDataStatus.isGraduated() && graduationStatusResponse.getProgramCompletionDate() != null && graduationDataStatus.getSchool() != null && graduationDataStatus.getSchool().getCertificateEligibility().equalsIgnoreCase("Y")) {
                    List<ProgramCertificateTranscript> certificateList = reportService.getCertificateList(gradResponse, graduationDataStatus, projectedOptionalGradResponse, exception);
                    for (ProgramCertificateTranscript certType : certificateList) {
                        if("FMR".equalsIgnoreCase(processName)) {
                            boolean createCertificate = false;
                            for(StudentOptionalProgram optionalProgram :projectedOptionalGradResponse) {
                                if ("F".equalsIgnoreCase(certType.getCertificateTypeCode()) && "FI".equalsIgnoreCase(optionalProgram.getOptionalProgramCode())) {
                                    createCertificate = true;
                                } else if (("E".equalsIgnoreCase(certType.getCertificateTypeCode()) || "EI".equalsIgnoreCase(certType.getCertificateTypeCode()))  && "DD".equalsIgnoreCase(optionalProgram.getOptionalProgramCode())) {
                                    createCertificate = true;
                                } else if (("A".equalsIgnoreCase(certType.getCertificateTypeCode()) || "AI".equalsIgnoreCase(certType.getCertificateTypeCode()))  && "DD".equalsIgnoreCase(optionalProgram.getOptionalProgramCode())) {
                                    createCertificate = true;
                                }
                            }
                            if(createCertificate) {
                                reportService.saveStudentCertificateReportJasper(graduationStatusResponse, graduationDataStatus, certType, false);
                                graduationDataStatus.getSchool().setSchoolCategoryLegacyCode(certType.getSchoolCategoryCode());
                                graduationDataStatus.getStudentCertificatesTranscript().addCertificateType(GradCertificateType.builder()
                                        .code(certType.getCertificateTypeCode())
                                        .description(certType.getCertificateTypeLabel())
                                        .build());
                            }
                        } else {
                            reportService.saveStudentCertificateReportJasper(graduationStatusResponse, graduationDataStatus, certType, false);
                            graduationDataStatus.getSchool().setSchoolCategoryLegacyCode(certType.getSchoolCategoryCode());
                            graduationDataStatus.getStudentCertificatesTranscript().addCertificateType(GradCertificateType.builder()
                                    .code(certType.getCertificateTypeCode())
                                    .description(certType.getCertificateTypeLabel())
                                    .build());
                            log.debug("**** Saved Certificates: {} ****", certType.getCertificateTypeCode());
                        }
                    }
                }

                if ((graduationDataStatus.getStudentCourses().getStudentCourseList() == null || graduationDataStatus.getStudentCourses().getStudentCourseList().isEmpty()) && (graduationDataStatus.getStudentAssessments().getStudentAssessmentList() == null || graduationDataStatus.getStudentAssessments().getStudentAssessmentList().isEmpty())) {
                    log.debug("**** No Transcript Generated: ****");
                } else if (graduationDataStatus.getSchool() != null && graduationDataStatus.getSchool().getTranscriptEligibility().equalsIgnoreCase("Y")) {
                    reportService.saveStudentTranscriptReportJasper(data, graduationStatusResponse.getStudentID(), exception, graduationDataStatus.isGraduated(), "FMR".equalsIgnoreCase(processName));
                    if(data.getTranscript() != null && data.getTranscript().getTranscriptTypeCode() != null) {
                        String transcriptTypeCode = ObjectUtils.defaultIfNull(data.getTranscript().getTranscriptTypeCode().getCode(), "");
                        graduationDataStatus.getStudentCertificatesTranscript().setTranscriptTypeCode(transcriptTypeCode);
                    }
                    log.debug("**** Saved Reports: ****");
                }
            } catch (Exception e) {
                exception.setExceptionName("REPORT GENERATION FAILURE");
                exception.setExceptionDetails(e.getLocalizedMessage());
                return exception;
            }
        }
        return null;
    }
}
