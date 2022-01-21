package ca.bc.gov.educ.api.graduation.model.report;

import java.io.Serializable;

public class GradProgram implements Serializable {
    private static final long serialVersionUID = 2L;

    private Code code;

    public Code getCode() {
        return code;
    }

    public void setCode(Code value) {
        this.code = value;
    }
}
