package ca.bc.gov.educ.api.graduation.process;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ca.bc.gov.educ.api.graduation.model.dto.ProcessorData;

@Component
public class AlgorithmProcessFactory {

    private static final Logger logger = LoggerFactory.getLogger(AlgorithmProcessFactory.class);

    @SuppressWarnings("unchecked")
	public static AlgorithmProcess createProcess(AlgorithmProcessType processImplementation, ProcessorData data) {
    	Class<AlgorithmProcess> clazz;
        AlgorithmProcess pcs = null;

        try {
            clazz = (Class<AlgorithmProcess>) Class.forName("ca.bc.gov.educ.api.graduation.process." + processImplementation.getValue());
            pcs = clazz.getDeclaredConstructor(ProcessorData.class).newInstance(data);
            System.out.println("Class Created: " + pcs.getClass());
        } catch (Exception e) {
            logger.debug("ERROR: No Such Class: " + processImplementation);
            logger.debug("Message:" + Arrays.toString(e.getStackTrace()));
        }

        return pcs;
    }
}
