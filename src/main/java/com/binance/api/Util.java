package com.binance.api;

import org.apache.commons.codec.digest.HmacUtils;

import java.time.Instant;

public final class Util {
  private Util() {

  }

  public static long getCurrentTime() {
    return Instant.now().toEpochMilli();
  }

  public static String hmacSha256(String key, String value) {
    return HmacUtils.hmacSha256Hex(key, value);
  }

  public static String generateSigQuery(String method, String inputQuery, String key) {
    String query = String.format("%s%stimestamp=%s",
      inputQuery,
      inputQuery.isEmpty() ? "" : "&",
      getCurrentTime());
    return String.format("%s%s?%s&signature=%s",
        "https://www.binance.com/api/",
        method,
        query,
        hmacSha256(key, query)
    );
  }
}
