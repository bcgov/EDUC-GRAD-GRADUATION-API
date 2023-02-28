package ca.bc.gov.educ.api.graduation.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlgorithmProcessFactory {

	@Autowired
	ProjectedGradFinalMarksRegistrationsProcess projectedGradFinalMarksRegistrationsProcess;
	
	@Autowired
	ProjectedGradFinalMarksProcess projectedGradFinalMarksProcess;
	
	@Autowired
	ProjectedGradFinalMarksReportsProcess projectedGradFinalMarksReportsProcess;
	
	@Autowired
	GraduateStudentProcess graduateStudentProcess;
	
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmProcessFactory.class);
    
	public AlgorithmProcess createProcess(AlgorithmProcessType processImplementation) {
        AlgorithmProcess pcs = null;
        switch(processImplementation.name()) {        
	        case "REGFM":
	        	logger.debug("\n************* PROJECTED (REGFM): Graduating Student START  ************");
	        	pcs = projectedGradFinalMarksRegistrationsProcess;
	        	break;
	        case "FM":
	        	logger.debug("\n************* PROJECTED (FM): Graduating Student START  ************");
	        	pcs = projectedGradFinalMarksProcess;
	        	break;
	        case "FMR":
	        	logger.debug("\n************* PROJECTED (FMR): Graduating Student START  ************");
	        	pcs = projectedGradFinalMarksReportsProcess;
	        	break;
	        case "GS":
	        	logger.debug("\n************* Graduating Student START  ************");
	        	pcs = graduateStudentProcess;
	        	break;
	        default:
	        	break;
        }
        return pcs;
    }
}
