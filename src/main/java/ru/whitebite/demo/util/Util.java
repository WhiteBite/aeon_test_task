
package ru.whitebite.demo.util;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

public class Util {

  private Util() {
    throw new UnsupportedOperationException("Cannot instantiate a Util class");
  }

  public static String generateRandomUuid() {
    return UUID.randomUUID().toString();
  }

  public static String getClientIP(HttpServletRequest request) {
    final String xfHeader = request.getHeader("X-Forwarded-For");
    if (xfHeader != null) {
      return xfHeader.split(",")[0];
    }
    return request.getRemoteAddr();
  }
}
