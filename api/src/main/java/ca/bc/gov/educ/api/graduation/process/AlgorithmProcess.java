package ca.bc.gov.educ.api.graduation.process;

import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.graduation.model.dto.ProcessorData;

@Component
public interface AlgorithmProcess {

	ProcessorData fire();

    public void setInputData(ProcessorData inputData);
}
