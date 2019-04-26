package com.netapsys.jira.plugins.cloneandmove.helper;

import com.atlassian.jira.util.BuildUtilsInfo;

public class DefaultBuildVersionHelper
    implements BuildVersionHelper {

  private static BuildUtilsInfo buildUtilsInfo;

  public DefaultBuildVersionHelper(BuildUtilsInfo buildUtilsInfo) {
    this.buildUtilsInfo = buildUtilsInfo;
  }

  public String getVersion() {
    return buildUtilsInfo.getVersion();
  }

  public String getCurrentBuildNumber() {
    return buildUtilsInfo.getCurrentBuildNumber();
  }
}