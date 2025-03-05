package ca.bc.gov.educ.api.graduation.constants;

import lombok.Getter;

@Getter
public enum DistrictContactTypeCodes {
  OL_LEARN("OL_LEARN"),
  SUPER("SUPER"),
  CHAIR("CHAIR"),
  SECRETARY("SECRETARY"),
  ADMN_ASSIS("ADMN_ASSIS"),
  INDIGENOUS("INDIGENOUS"),
  CUSTORDER("CUSTORDER"),
  EARL_LEARN("EARL_LEARN"),
  FACILITIES("FACILITIES"),
  FINANCIAL("FINANCIAL"),
  FRENCH("FRENCH"),
  INTERNAT("INTERNAT"),
  LITERACY("LITERACY"),
  MYED("MYED"),
  INCLUSIVE("INCLUSIVE"),
  TRANSPORT("TRANSPORT"),
  DATA_COLLECTION_1701("1701"),
  STUDREGIS("STUDREGIS");


  private final String code;

  DistrictContactTypeCodes(String code) {this.code = code;}

}
