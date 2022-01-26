package ca.bc.gov.educ.api.graduation.model.report;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class Transcript implements Serializable {

    private static final long serialVersionUID = 2L;

    private String interim;
    private Date issueDate;
    private Code transcriptTypeCode;
    private List<TranscriptResult> results;

    public String getInterim() {
        return interim;
    }

    public void setInterim(String value) {
        this.interim = value;
    }

    @JsonFormat(pattern="yyyy-MM-dd")
    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date value) {
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
