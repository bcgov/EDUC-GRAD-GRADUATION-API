package ca.bc.gov.educ.api.graduation.constants;

import lombok.Getter;

@Getter
public enum ReportTypeCodes {
  TVRGRAD("TVRGRAD"),
  TVRNONGRAD("TVRNONGRAD"),
  GRADREGARC("GRADREGARC"),
  NONGRADREGARC("NONGRADREGARC"),
  NONGRADPRJARC("NONGRADPRJARC"),
  NONGRADPRJ("NONGRADPRJ"),
  ACHV("ACHV"),
  GRADREG("GRADREG"),
  DISTREP_SC("DISTREP_SC"),
  DISTREP_YE_SC("DISTREP_YE_SC"),
  DISTREP_YE_SD("DISTREP_YE_SD"),
  NONGRADDISTREP_SC("NONGRADDISTREP_SC"),
  NONGRADDISTREP_SD("NONGRADDISTREP_SD"),
  GRADPRJ("GRADPRJ"),
  GRADPRJARC("GRADPRJARC"),
  ADDRESS_LABEL_SCHL("ADDRESS_LABEL_SCHL"),
  ADDRESS_LABEL_YE("ADDRESS_LABEL_YE"),
  ADDRESS_LABEL_SCH_YE("ADDRESS_LABEL_SCH_YE"),
  ADDRESS_LABEL_PSI("ADDRESS_LABEL_PSI"),
  NONGRADREG("NONGRADREG"),
  DISTREP_SD("DISTREP_SD");

  private final String code;

  ReportTypeCodes(String code) {this.code = code;}

}
