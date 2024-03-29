package ca.bc.gov.educ.api.graduation.model.report;


import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class Transcript implements Serializable {

    private static final long serialVersionUID = 2L;

    private String interim;
    private LocalDate issueDate;
    private Code transcriptTypeCode;
    private List<TranscriptResult> results;

    public String getInterim() {
        return interim;
    }

    public void setInterim(String value) {
        this.interim = value;
    }

    @JsonProperty("code")
    @JsonDeserialize(as = Code.class)
    public Code getTranscriptTypeCode() {
        return transcriptTypeCode;
    }

    public void setTranscriptTypeCode(Code code) {
        this.transcriptTypeCode = code;
    }

    @JsonFormat(pattern= EducGraduationApiConstants.DEFAULT_DATE_FORMAT)
    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate value) {
        this.issueDate = value;
    }

    @JsonProperty("results")
    public List<TranscriptResult> getResults() {
        return results;
    }

    public void setResults(List<TranscriptResult> value) {
        this.results = value;
    }

}
