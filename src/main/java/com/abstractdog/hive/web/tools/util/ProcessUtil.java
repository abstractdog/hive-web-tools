package com.abstractdog.hive.web.tools.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ProcessUtil {

  public static int runProcess(Process p, StringBuilder outBuilder, StringBuilder errorBuilder) throws Exception {
    try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
      BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
      String line;

      while ((line = error.readLine()) != null) {
        errorBuilder.append(line + "\n");
      }
      while ((line = input.readLine()) != null) {
        outBuilder.append(line + "\n");
      }
    }
    int success = p.waitFor();
    return success;
  }
}
