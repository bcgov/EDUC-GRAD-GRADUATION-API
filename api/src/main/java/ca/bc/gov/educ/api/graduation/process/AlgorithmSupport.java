package ca.bc.gov.educ.api.graduation.process;

import ca.bc.gov.educ.api.graduation.model.dto.*;
import ca.bc.gov.educ.api.graduation.model.report.ReportData;
import ca.bc.gov.educ.api.graduation.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlgorithmSupport {

    private static Logger logger = LoggerFactory.getLogger(AlgorithmSupport.class);

    @Autowired
    ReportService reportService;

    public boolean checkForErrors(GraduationData graduationDataStatus, AlgorithmResponse algorithmResponse, ProcessorData processorData) {
        if (graduationDataStatus != null && graduationDataStatus.getException() != null && graduationDataStatus.getException().getExceptionName() != null) {
            logger.info("**** Grad Algorithm Has Errors: ****");
            algorithmResponse.setException(graduationDataStatus.getException());
            processorData.setAlgorithmResponse(algorithmResponse);
            return true;
        }
        return false;
    }

    public void createReportNCert(GraduationData graduationDataStatus, GraduationStudentRecord graduationStatusResponse, GraduationStudentRecord gradResponse, List<StudentOptionalProgram> projectedOptionalGradResponse, ExceptionMessage exception, ReportData data,ProcessorData processorData) {
        if(graduationDataStatus != null) {
            if (graduationDataStatus.isGraduated() && graduationStatusResponse.getProgramCompletionDate() != null) {
                List<ProgramCertificateTranscript> certificateList = reportService.getCertificateList(gradResponse, graduationDataStatus, projectedOptionalGradResponse, processorData.getAccessToken(), exception);
                for (ProgramCertificateTranscript certType : certificateList) {
                    reportService.saveStudentCertificateReportJasper(graduationStatusResponse, graduationDataStatus, processorData.getAccessToken(), certType, exception);
                    logger.info("**** Saved Certificates: {} ****",certType.getCertificateTypeCode());
                }
            }

            if (graduationDataStatus.getStudentCourses().getStudentCourseList().isEmpty() && graduationDataStatus.getStudentAssessments().getStudentAssessmentList().isEmpty()) {
                logger.info("**** No Transcript Generated: ****");
            } else {
                reportService.saveStudentTranscriptReportJasper(data, processorData.getAccessToken(), graduationStatusResponse.getStudentID(), exception, graduationDataStatus.isGraduated());
                logger.info("**** Saved Reports: ****");
            }
        }
    }
}
