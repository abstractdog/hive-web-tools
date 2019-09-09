package com.abstractdog.hive.web.tools.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class HivePtestResultSaver {
  private static final Logger log = LoggerFactory.getLogger(HivePtestResultSaver.class);

  private static final String jobUrl = "https://builds.apache.org/job/PreCommit-HIVE-Build";
  private static final String ptestJobApiUrl = jobUrl
      + "/%d/testReport/api/json?tree=duration,skipCount,failCount,passCount,suites[name,duration,cases[age,className,duration,failedSince,name,skipped,status]]";
  private static final String lastNPtestApiUrl = jobUrl + "/lastCompletedBuild/api/json?tree=id";

  @Value("${hive.web.tools.ptest.output}")
  private File outputFolder;

  @Scheduled(fixedRate = 1000 * 60 * 60)
  public void reportCurrentTime() throws Exception {
    outputFolder.mkdirs();

    log.info("Start saving ptest results");

    int lastId = Integer.parseInt(loadJsonFromUrl(lastNPtestApiUrl).get("id").asText());
    log.info("Last ptest result is: " + lastId);

    for (int i = lastId; i >= lastId - 10; i--) {
      if (resultNotYetSaved(i)) {
        savePtestResult(i);
      }
    }

    log.info("Finished saving ptest results");
  }

  private boolean resultNotYetSaved(int id) {
    return !getResultFile(id).exists();
  }

  private File getResultFile(int id) {
    return new File(outputFolder, String.format("testReport_%d.json", id));
  }

  private void savePtestResult(int id) throws Exception {
    log.info("Saving ptest result: " + id);
    File resultFile = getResultFile(id);

    JsonNode node = null;
    try {
      node = loadJsonFromUrl(String.format(ptestJobApiUrl, id));
    } catch (FileNotFoundException e) {
      log.info("Result not found for id: " + id);
    }
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(resultFile, node == null ? mapper.createObjectNode() : node);
  }

  private JsonNode loadJsonFromUrl(String strUrl) throws Exception {
    URL url = new URL(strUrl);
    URLConnection request = url.openConnection();
    request.connect();

    return new ObjectMapper().readTree(((InputStream) request.getContent()));
  }

  public String streamToString(InputStream stream) throws Exception {
    String str = null;
    try (Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8.name())) {
      str = scanner.useDelimiter("\\A").next();
    }
    stream.close();
    return str;
  }
}
