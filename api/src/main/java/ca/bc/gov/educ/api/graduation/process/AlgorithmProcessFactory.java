package ca.bc.gov.educ.api.graduation.process;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlgorithmProcessFactory {

	ProjectedGradFinalMarksRegistrationsProcess projectedGradFinalMarksRegistrationsProcess;
	ProjectedGradFinalMarksProcess projectedGradFinalMarksProcess;
	ProjectedGradFinalMarksReportsProcess projectedGradFinalMarksReportsProcess;
	GraduateStudentProcess graduateStudentProcess;

	@Autowired
	public AlgorithmProcessFactory(ProjectedGradFinalMarksRegistrationsProcess projectedGradFinalMarksRegistrationsProcess,
								   ProjectedGradFinalMarksProcess projectedGradFinalMarksProcess,
								   ProjectedGradFinalMarksReportsProcess projectedGradFinalMarksReportsProcess,
								   GraduateStudentProcess graduateStudentProcess) {
		this.projectedGradFinalMarksRegistrationsProcess = projectedGradFinalMarksRegistrationsProcess;
		this.projectedGradFinalMarksProcess = projectedGradFinalMarksProcess;
		this.projectedGradFinalMarksReportsProcess = projectedGradFinalMarksReportsProcess;
		this.graduateStudentProcess = graduateStudentProcess;
	}

    public AlgorithmProcess createProcess(AlgorithmProcessType processImplementation) {
        AlgorithmProcess pcs = null;
		switch (processImplementation.name()) {
			case "REGFM" -> {
				log.debug("\n************* PROJECTED (REGFM): Graduating Student START  ************");
				pcs = projectedGradFinalMarksRegistrationsProcess;
			}
			case "FM" -> {
				log.debug("\n************* PROJECTED (FM): Graduating Student START  ************");
				pcs = projectedGradFinalMarksProcess;
			}
			case "FMR" -> {
				log.debug("\n************* PROJECTED (FMR): Graduating Student START  ************");
				pcs = projectedGradFinalMarksReportsProcess;
			}
			case "GS" -> {
				log.debug("\n************* Graduating Student START  ************");
				pcs = graduateStudentProcess;
			}
			default ->
				log.debug("\n************ No matched Algorithm Process is found for {} ************", processImplementation.name());
		}
        return pcs;
    }
}
