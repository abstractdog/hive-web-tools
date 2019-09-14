package com.abstractdog.hive.web.tools.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.springframework.stereotype.Service;

import com.abstractdog.hive.web.tools.util.ProcessUtil;

@Service
public class HiveParserService {
  public enum HiveVersion {
    v0_14_0("0.14.0"), v1_2_2("1.2.2"), v2_3_3("2.3.3"), v3_1_2("3.1.2");

    private String versionName;

    HiveVersion(String versionName) {
      this.versionName = versionName;
    }

    public String getVersionName() {
      return versionName;
    }
  }

  public HiveParserService() {
    // TODO Auto-generated constructor stub
  }

  public static void main(String[] args) throws Exception {
    String query = args[0];
    ASTNode rootNode = new HiveParserService().parse(query);

    System.out.println(rootNode.toStringTree());
  }

  public ASTNode parse(String query) throws ParseException {
    return new ParseDriver().parse(query);
  }

  public String callInSeparateJvm(String query, String version) throws Exception {
    ProcessBuilder pb =
        new ProcessBuilder(new String[] { "java", "-cp", getClassPath(version), getClass().getCanonicalName(), query });
    pb.redirectErrorStream(true);
    Process p = pb.start();

    StringBuilder output = new StringBuilder();
    StringBuilder error = new StringBuilder();

    ProcessUtil.runProcess(p, output, error);

    System.out.println(output.toString());

    return output.toString();
  }

  private String getClassPath(String version) throws Exception {
    List<String> classPathItems = new ArrayList<>();

    File hiveExecJar = new File(
        this.getClass().getClassLoader().getResource(String.format("hive/hive-exec-%s.jar", version)).getFile());
    classPathItems.add(hiveExecJar.getCanonicalPath());

    classPathItems.add(System.getProperty("hive.tools.classpath"));

    return String.join(":", classPathItems);
  }
}
