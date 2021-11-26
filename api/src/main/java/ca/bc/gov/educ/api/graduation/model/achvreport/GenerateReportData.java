package ca.bc.gov.educ.api.graduation.model.achvreport;

import lombok.Data;

@Data
public class GenerateReportData {
    private AchvReportData data;
    private AchvReportOptions options;
}
