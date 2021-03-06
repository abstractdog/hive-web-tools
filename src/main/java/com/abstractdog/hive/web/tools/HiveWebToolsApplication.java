package com.abstractdog.hive.web.tools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HiveWebToolsApplication {
  public static void main(String[] args) {
    SpringApplication.run(HiveWebToolsApplication.class, args);
  }
}