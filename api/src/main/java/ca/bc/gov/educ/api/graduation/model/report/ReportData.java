package ca.bc.gov.educ.api.graduation.model.report;

import ca.bc.gov.educ.api.graduation.model.dto.institute.District;
import ca.bc.gov.educ.api.graduation.model.dto.ExceptionMessage;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants.DEFAULT_DATE_FORMAT;

@Data
@Component
@XmlType(name = "")
@XmlRootElement(name = "generateReport")
@XmlSeeAlso({
		Student.class,
		School.class,
		District.class,
		Transcript.class,
		GradProgram.class,
		NonGradReason.class,
		Certificate.class
})
@JsonSubTypes({
		@JsonSubTypes.Type(value = Student.class),
		@JsonSubTypes.Type(value = School.class),
		@JsonSubTypes.Type(value = District.class),
		@JsonSubTypes.Type(value = Transcript.class),
		@JsonSubTypes.Type(value = GradProgram.class),
		@JsonSubTypes.Type(value = NonGradReason.class),
		@JsonSubTypes.Type(value = Certificate.class),
		@JsonSubTypes.Type(value = OptionalProgram.class),
		@JsonSubTypes.Type(value = GradRequirement.class),
		@JsonSubTypes.Type(value = Exam.class),
		@JsonSubTypes.Type(value = Assessment.class),
		@JsonSubTypes.Type(value = AssessmentResult.class),
		@JsonSubTypes.Type(value = AchievementCourse.class)
})
public class ReportData implements Serializable {

	private static final long serialVersionUID = 2L;

	@JsonDeserialize(as = Student.class)
	private Student student;
	@JsonDeserialize(as = School.class)
	private School school;
	@JsonDeserialize(as = District.class)
	private District district;
	private String logo;
	@JsonDeserialize(as = Transcript.class)
	private Transcript transcript;
	@JsonDeserialize(as = Assessment.class)
	private Assessment assessment;
	@JsonDeserialize(as = GradProgram.class)
	private GradProgram gradProgram;
	@JsonDeserialize(as = GraduationData.class)
	private GraduationData graduationData;
	private String gradMessage;
	@JsonFormat(pattern= DEFAULT_DATE_FORMAT)
	private String updateDate;
	@JsonDeserialize(as = Certificate.class)
	private Certificate certificate;
	@JsonDeserialize(as = GraduationStatus.class)
	private GraduationStatus graduationStatus;
	private String orgCode;
	@JsonFormat(pattern=DEFAULT_DATE_FORMAT)
	private Date issueDate;

	private String reportNumber;
	private String reportTitle;
	private String reportSubTitle;

	private List<NonGradReason> nonGradReasons;
	private List<AchievementCourse> studentCourses;
	private List<Exam> studentExams;
	private List<OptionalProgram> optionalPrograms;

	private List<School> schools;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Map<String, String> parameters = new HashMap<>();

	private ExceptionMessage exception;
}
