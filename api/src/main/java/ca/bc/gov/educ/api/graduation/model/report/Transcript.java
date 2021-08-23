package ca.bc.gov.educ.api.graduation.model.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Date;
import java.util.List;

public class Transcript {
    private String interim;
    private String issueDate;
    private List<TranscriptResult> results;

    public String getInterim() {
        return interim;
    }

    public void setInterim(String value) {
        this.interim = value;
    }

    @JsonFormat(pattern="yyyy-MM-dd")
    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String value) {
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
