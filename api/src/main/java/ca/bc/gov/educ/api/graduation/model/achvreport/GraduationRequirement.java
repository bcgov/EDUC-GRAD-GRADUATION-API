package ca.bc.gov.educ.api.graduation.model.achvreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraduationRequirement {
    private String rule;
    private String description;
    private List<CourseDetails> courseDetails;
}
