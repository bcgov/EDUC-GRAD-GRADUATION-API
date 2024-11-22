package ca.bc.gov.educ.api.graduation.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

/**
 * The type JWT util.
 */
public class JwtUtil {

  private JwtUtil() {
  }

  /**
   * Gets username string from object.
   *
   * @param jwt the JWT
   * @return the username string from jwt
   */
  public static String getUsername(Jwt jwt) {
    return (String) jwt.getClaims().get("preferred_username");
  }

  /**
   * Gets email string from object.
   *
   * @param jwt the JWT
   * @return the username string from jwt
   */
  public static String getEmail(Jwt jwt) {
    return (String) jwt.getClaims().get("email");
  }

  /**
   * Gets name string from object.
   *
   * @param jwt the JWT
   * @return the username string from jwt
   */
  public static String getName(Jwt jwt) {
    StringBuilder sb = new StringBuilder();
    if (isServiceAccount(jwt.getClaims())) {
      sb.append("Batch Process");
    } else {
      String givenName = (String) jwt.getClaims().get("given_name");
      if (StringUtils.isNotBlank(givenName)) {
        sb.append(givenName.charAt(0));
      }
      String familyName = (String) jwt.getClaims().get("family_name");
      sb.append(familyName);
    }
    return sb.toString();
  }

  /**
   * Gets name string
   * => If it is service account, get it from request header.  Otherwise, get username from jwt token
   *
   * @param jwt the JWT
   * @param request the Request Header
   * @return the username string
   */
  public static String getName(Jwt jwt, HttpServletRequest request) {
    StringBuilder sb = new StringBuilder();
    if (isServiceAccount(jwt.getClaims())) {
      sb.append(getUserNameString(request));
    } else {
      String givenName = (String) jwt.getClaims().get("given_name");
      if (StringUtils.isNotBlank(givenName)) {
        sb.append(givenName.charAt(0));
      }
      String familyName = (String) jwt.getClaims().get("family_name");
      sb.append(familyName);
    }
    return sb.toString();
  }

  private static String getUserNameString(HttpServletRequest request) {
    val username = request.getHeader(EducGraduationApiConstants.USERNAME);
    if (StringUtils.isNotBlank(username)) {
      return username;
    } else {
      return "Batch Process";
    }
  }

  public static boolean isServiceAccount(Map<String, Object> claims) {
    return !claims.containsKey("family_name");
  }
}
