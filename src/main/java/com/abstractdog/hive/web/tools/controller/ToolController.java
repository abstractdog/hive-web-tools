package com.abstractdog.hive.web.tools.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
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
  public String runLipwig(@RequestParam Map<String, Object> params) throws Exception {

    File inputFile = File.createTempFile("lipwig-input", null);
    File outputFile = File.createTempFile("lipwig-output", ".svg");

    Files.write(params.get("plan").toString().getBytes(), inputFile);

    try {
      ProcessBuilder pb = new ProcessBuilder(String.format("%s -i %s -o %s", lipwigPath,
          inputFile.getAbsolutePath(), outputFile.getAbsolutePath()));
      pb.directory(new File(lipwigPath).getParentFile());

      Process p = pb.start();

      try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
        String line;

        while ((line = error.readLine()) != null) {
          System.out.println(line);
        }
        while ((line = input.readLine()) != null) {
          System.out.println(line);
        }
      }

    } catch (Exception err) {
      err.printStackTrace();
    }

    File finalOutputFile = new File(lipwigOutputFolder, outputFile.getName());
    Files.move(outputFile, finalOutputFile);

    return String.format("%s%s", lipwigOutputPath, finalOutputFile.getName());
  }
}
