package ca.bc.gov.educ.api.graduation.model.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.stereotype.Component;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@XmlType(name = "")
@XmlRootElement(name = "generateReport")
@XmlSeeAlso({
		Student.class,
		School.class,
		Transcript.class,
		GradProgram.class,
		NonGradReason.class,
		Certificate.class
})
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
//@JsonPropertyOrder(alphabetic = true)
//@JsonRootName("generateReport")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = Student.class),
		@JsonSubTypes.Type(value = School.class),
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
	@JsonFormat(pattern="yyyy-MM-dd")
	private String updateDate;
	@JsonDeserialize(as = Certificate.class)
	private Certificate certificate;
	@JsonDeserialize(as = GraduationStatus.class)
	private GraduationStatus graduationStatus;
	private String orgCode;
	@JsonFormat(pattern="yyyy-MM-dd")
	private Date issueDate;

	private List<NonGradReason> nonGradReasons;
	private List<AchievementCourse> studentCourses;
	private List<Exam> studentExams;
	private List<OptionalProgram> optionalPrograms;

	@JsonIgnore
	private Map<String, String> parameters = new HashMap<>();
}
