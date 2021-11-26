package ca.bc.gov.educ.api.graduation.model.achvreport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NonGraduationReason {

    private String rule;
    private String description;
}
