package com.binance.api.messages;

import org.apache.commons.codec.digest.HmacUtils;

import java.time.Instant;
import java.util.Arrays;

public final class MessageUtil {
  private static final String API_BASE = "https://www.binance.com/";

  private MessageUtil() {

  }

  private static long getCurrentTime() {
    return Instant.now().toEpochMilli();
  }

  private static String hmacSha256(String key, String value) {
    return HmacUtils.hmacSha256Hex(key, value);
  }

  static String generateSigQuery(String method, String key, String... params) {
    return generateQuery(method, getSignedParamsBuilder(key, params));
  }

  static String generateQuery(String method, String... params) {
    return generateQuery(method, getParamsBuilder(params));
  }

  private static StringBuilder getBaseBuilder(String method) {
    StringBuilder builder = new StringBuilder(API_BASE);
    builder.append(method);
    return builder;
  }

  private static StringBuilder getParamsBuilder(String... params) {
    assert params.length % 2 == 0;
    StringBuilder builder = new StringBuilder();
    if (params.length > 0) {
      builder.append(params[0]).append('=').append(params[1]);
      for (int i = 2; i < params.length; i += 2) {
        builder.append('&').append(params[i]).append('=').append(params[i + 1]);
      }
    }
    return builder;
  }

  private static StringBuilder getSignedParamsBuilder(String key, String... params) {
    String[] extended = Arrays.copyOf(params, params.length + 2);
    extended[params.length] = "timestamp";
    extended[params.length + 1] = Long.toString(getCurrentTime());
    StringBuilder paramsBuilder = getParamsBuilder(extended);
    String query = paramsBuilder.toString();
    paramsBuilder.append("&signature=").append(hmacSha256(key, query));
    return paramsBuilder;
  }

  private static String generateQuery(String method, StringBuilder params) {
    return getBaseBuilder(method).append(new StringBuilder("?").append(params)).toString();
  }
}
