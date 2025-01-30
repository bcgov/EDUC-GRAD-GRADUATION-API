package ca.bc.gov.educ.api.graduation.constants;

import lombok.Getter;

@Getter
public enum AddressTypeCodes {
  MAILING("MAILING"),
  PHYSICAL("PHYSICAL");

  private final String code;

  AddressTypeCodes(String code) {this.code = code;}

}
