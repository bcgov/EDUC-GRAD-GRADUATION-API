package ca.bc.gov.educ.api.graduation.process;

import ca.bc.gov.educ.api.graduation.model.dto.AlgorithmResponse;
import ca.bc.gov.educ.api.graduation.model.dto.ExceptionMessage;
import ca.bc.gov.educ.api.graduation.model.dto.ProcessorData;
import ca.bc.gov.educ.api.graduation.service.GradAlgorithmService;
import ca.bc.gov.educ.api.graduation.service.GradStatusService;
import ca.bc.gov.educ.api.graduation.service.OptionalProgramService;
import ca.bc.gov.educ.api.graduation.service.ReportService;
import ca.bc.gov.educ.api.graduation.util.GradValidation;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseProcess implements AlgorithmProcess {

    @Autowired
    GradStatusService gradStatusService;

    @Autowired
    GradAlgorithmService gradAlgorithmService;

    @Autowired
    OptionalProgramService optionalProgramService;

    @Autowired
    ReportService reportService;

    @Autowired
    GradValidation validation;

    @Autowired
    AlgorithmSupport algorithmSupport;

    protected boolean checkExceptions(ExceptionMessage exception, AlgorithmResponse algorithmResponse, ProcessorData processorData) {
        if (exception != null && exception.getExceptionName() != null) {
            algorithmResponse.setException(exception);
            processorData.setAlgorithmResponse(algorithmResponse);
            return true;
        }
        return false;
    }
}
