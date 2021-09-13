package ca.bc.gov.educ.api.graduation.process;

public enum AlgorithmProcessType {
	REGFM ("ProjectedGradFinalMarksRegistrationsProcess"),
	FM ("ProjectedGradFinalMarksProcess"),
	FMR ("ProjectedGradReportsProcess"),
    GS ("GraduateStudentProcess");

    private final String value;

    AlgorithmProcessType(String value){
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
