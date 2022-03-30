package ca.bc.gov.educ.api.graduation.process;

import ca.bc.gov.educ.api.graduation.model.dto.ProcessorData;
import org.springframework.stereotype.Component;

@Component
public interface AlgorithmProcess {
	ProcessorData fire(ProcessorData processorData);
}
