package ca.bc.gov.educ.api.graduation.mapper;

import ca.bc.gov.educ.api.graduation.model.dto.institute.School;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, StringMapper.class})
@SuppressWarnings("squid:S1214")
public interface SchoolMapper {

  SchoolMapper mapper = Mappers.getMapper(SchoolMapper.class);

  @Mapping(target = "name", source = "displayName")
  ca.bc.gov.educ.api.graduation.model.report.School toSchoolReport(School entity);
}
