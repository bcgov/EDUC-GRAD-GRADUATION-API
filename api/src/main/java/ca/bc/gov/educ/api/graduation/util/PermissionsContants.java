package ca.bc.gov.educ.api.graduation.util;

public interface PermissionsContants {
	String _PREFIX = "hasAuthority('";
	String _SUFFIX = "')";

	String GRADUATE_STUDENT = _PREFIX + "SCOPE_UPDATE_GRAD_GRADUATION_STATUS" + _SUFFIX + " and " + _PREFIX + "SCOPE_RUN_GRAD_ALGORITHM" + _SUFFIX;
	String GRADUATE_DATA = _PREFIX + "SCOPE_GET_GRADUATION_DATA" + _SUFFIX;
}
