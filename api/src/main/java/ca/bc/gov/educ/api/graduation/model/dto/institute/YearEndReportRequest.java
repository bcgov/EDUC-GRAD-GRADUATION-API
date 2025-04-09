package ca.bc.gov.educ.api.graduation.model.dto.institute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class YearEndReportRequest {
  List<UUID> schoolIds;
  List<UUID> districtIds;
  List<String> schoolCategoryCodes;
}
