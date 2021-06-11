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
	        	logger.info("\n************* PROJECTED (REGFM): Graduating Student START  ************");
	        	pcs = projectedGradFinalMarksRegistrationsProcess;
	        	break;
	        case "FM":
	        	logger.info("\n************* PROJECTED (FM): Graduating Student START  ************");
	        	pcs = projectedGradFinalMarksProcess;
	        	break;
	        case "FMR":
	        	logger.info("\n************* PROJECTED (FMR): Graduating Student START  ************");
	        	pcs = projectedGradFinalMarksReportsProcess;
	        	break;
	        case "GS":
	        	logger.info("\n************* Graduating Student START  ************");
	        	pcs = graduateStudentProcess;
	        	break;
	        default:
	        	break;
        }
        return pcs;
    }
}
