package ca.bc.gov.educ.api.graduation.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class SchoolReportRequest {

	private List<GraduationStudentRecord> studentList;
}
