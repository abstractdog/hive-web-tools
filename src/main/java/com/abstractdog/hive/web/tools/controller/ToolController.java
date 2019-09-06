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
  public String runLipwig(@RequestParam Map<String, Object> params)
      throws Exception {

    File inputFile = File.createTempFile("lipwig-input", null);
    File outputFile = File.createTempFile("lipwig-output", ".svg");

    try {
      // enter code here

      Process p = Runtime.getRuntime().exec(Paths.get(String.format("%s -i %s -o %s", lipwigPath,
          inputFile.getAbsolutePath(), outputFile.getAbsolutePath())).toString());

      // enter code here

      try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        String line;

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
