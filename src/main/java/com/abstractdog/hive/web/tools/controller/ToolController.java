package com.abstractdog.hive.web.tools.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abstractdog.hive.web.tools.service.HiveParserService;
import com.abstractdog.hive.web.tools.util.ProcessUtil;
import com.google.common.io.Files;

@RestController
@RequestMapping(value = "/tool")
public class ToolController {

  @Autowired
  private HiveParserService hiveParserService;

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
      StringBuilder output = new StringBuilder();
      StringBuilder error = new StringBuilder();

      success = ProcessUtil.runProcess(p, output, error) == 0;

      sb.append(error);
      sb.append(output);

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

  @RequestMapping(value = "/parse", method = RequestMethod.POST)
  public Map<String, Object> parseQuery(@RequestParam String query, @RequestParam(required = false) String version)
      throws Exception {
    if (version == null) {
      version = HiveParserService.HiveVersion.v3_1_2.getVersionName();
    }
    String result = hiveParserService.callInSeparateJvm(query, version);

    Map<String, Object> response = new HashMap<String, Object>();
    response.put("success", true);
    response.put("message", result);
    response.put("version", version);
    response.put("query", query);

    return response;
  }
}
