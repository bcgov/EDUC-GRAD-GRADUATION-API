package ca.bc.gov.educ.api.graduation.model.report;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GraduationData implements Serializable {
    private static final long serialVersionUID = 2L;

    private LocalDate graduationDate;
    private boolean honorsFlag;
    private boolean dogwoodFlag;
    private List<String> programCodes;
    private List<String> programNames;
    private String totalCreditsUsedForGrad;

    @JsonFormat(pattern="yyyy-MM-dd")
    public LocalDate getGraduationDate() {
        return graduationDate;
    }

    public void setGraduationDate(LocalDate value) {
        this.graduationDate = value;
    }

    public boolean getHonorsFlag() {
        return honorsFlag;
    }

    public void setHonorsFlag(boolean value) {
        this.honorsFlag = value;
    }

    public boolean getDogwoodFlag() {
        return dogwoodFlag;
    }

    public void setDogwoodFlag(boolean value) {
        this.dogwoodFlag = value;
    }

    public List<String> getProgramCodes() {
        if(programCodes == null) {
            programCodes = new ArrayList<>();
        }
        return programCodes;
    }

    public void setProgramCodes(List<String> value) {
        this.programCodes = value;
    }

    public List<String> getProgramNames() {
        if(programNames == null) {
            programNames = new ArrayList<>();
        }
        return programNames;
    }

    public void setProgramNames(List<String> value) {
        this.programNames = value;
    }

    public String getTotalCreditsUsedForGrad() {
        return totalCreditsUsedForGrad;
    }

    public void setTotalCreditsUsedForGrad(String value) {
        this.totalCreditsUsedForGrad = value;
    }
}
