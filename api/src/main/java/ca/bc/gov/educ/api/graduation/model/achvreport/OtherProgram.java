package ca.bc.gov.educ.api.graduation.model.achvreport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtherProgram {

    private String programCode;
    private String programName;

}
