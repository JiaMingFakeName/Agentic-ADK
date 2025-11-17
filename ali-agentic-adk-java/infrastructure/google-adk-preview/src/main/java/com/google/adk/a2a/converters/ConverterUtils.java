package com.google.adk.a2a.converters;

/**
 * @author dlancerc
 * @date 2025-07-28-21:56
 */
public class ConverterUtils {

  private static final String ADK_METADATA_KEY_PREFIX = "adk_";
  private static final String ADK_CONTEXT_ID_PREFIX = "ADK";
  private static final String ADK_CONTEXT_ID_SEPARATOR = "/";

  public static String getAdkMetadataKey(String key) {
    return ADK_METADATA_KEY_PREFIX + key;
  }
}
