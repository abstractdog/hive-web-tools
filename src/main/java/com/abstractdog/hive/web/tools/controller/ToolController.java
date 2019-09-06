package com.abstractdog.hive.web.tools.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.io.Files;

@RestController
@RequestMapping(value = "/tool")
public class ToolController {

  @Value("${hive.web.tools.lipwig.path}")
  private String lipwigPath;

  @Value("${hive.web.tools.lipwig.output.folder}")
  private String lipwigOutputFolder;

  @Value("${hive.web.tools.lipwig.output.path}")
  private String lipwigOutputPath;

  @RequestMapping(value = "/lipwig", method = RequestMethod.POST)
  public Map<String, Object> runLipwig(@RequestParam Map<String, Object> params) throws Exception {
    String plan = params.get("plan").toString();
    System.out.println(plan);

    boolean success = false;
    StringBuilder sb = new StringBuilder();

    File outputFile = File.createTempFile("lipwig-output", ".svg");
    File finalOutputFile = new File(lipwigOutputFolder, outputFile.getName());

    try {
      File inputFile = File.createTempFile("lipwig-input", null);

      Files.write(plan.getBytes(), inputFile);

      ProcessBuilder pb =
          new ProcessBuilder(lipwigPath, "-i", inputFile.getAbsolutePath(), "-o", outputFile.getAbsolutePath());
      pb.directory(new File(lipwigPath).getParentFile());
      System.out.println(pb.command());

      Process p = pb.start();

      try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
        String line;

        sb.append("ERROR OUT: \n");
        while ((line = error.readLine()) != null) {
          sb.append(line + "\n");
        }
        sb.append("SYSTEM OUT: \n");
        while ((line = input.readLine()) != null) {
          sb.append(line + "\n");
        }
      }
      success = p.waitFor() == 0;

      Files.move(outputFile, finalOutputFile);
    } catch (Exception err) {
      sb.append(err.getMessage());
      err.printStackTrace();
    }

    Map<String, Object> response = new HashMap<String, Object>();
    response.put("success", success);

    if (success) {
      response.put("message", String.format("%s%s", lipwigOutputPath, finalOutputFile.getName()));
    } else {
      response.put("message", sb.toString());
    }

    return response;
  }
}
