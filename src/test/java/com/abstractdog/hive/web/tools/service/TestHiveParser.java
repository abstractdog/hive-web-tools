package com.abstractdog.hive.web.tools.service;

import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.junit.Assert;
import org.junit.Test;

public class TestHiveParser {

  @Test
  public void testParse() throws ParseException {
    final ParseDriver driver = new ParseDriver();
    Assert.assertEquals(2, driver.parse("SELECT 1").getChildCount());
  }
}
